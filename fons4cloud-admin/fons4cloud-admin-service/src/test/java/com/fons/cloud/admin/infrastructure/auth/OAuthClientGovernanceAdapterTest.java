package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientQueryRequest;
import com.fons.cloud.auth.request.OauthClientRotateSecretRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OAuth Client 治理适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class OAuthClientGovernanceAdapterTest {

    @Mock
    private AdminOauthClientManagementClient oauthClientManagementClient;

    private OAuthClientGovernanceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OAuthClientGovernanceAdapter(oauthClientManagementClient);
    }

    @Test
    void validateShouldPassForCreateCommandWithoutSecret() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef("demo-client"),
                """
                        {"operation":"CREATE","clientId":"demo-client","scope":"all","authorizedGrantTypes":"password","additionalInformation":"{}","status":true}
                        """, null));

        assertThat(result.getPassed()).isTrue();
        assertThat(result.getNormalizedContentHash()).isNotBlank();
    }

    @Test
    void validateShouldRejectPlainSecretAndInvalidAdditionalInformation() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef("demo-client"),
                """
                        {"operation":"CREATE","clientId":"demo-client","clientSecret":"plain","additionalInformation":"not-json"}
                        """, null));

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getErrors()).extracting("code")
                .contains("CLIENT_SECRET_NOT_ALLOWED", "CLIENT_ADDITIONAL_INFORMATION_INVALID");
    }

    @Test
    void publishCreateShouldCallAuthRpcAndStoreOnlyMaskedSnapshot() {
        when(oauthClientManagementClient.query(any(OauthClientQueryRequest.class)))
                .thenReturn(R.failed("NOT_FOUND", "not found"))
                .thenReturn(R.ok(clientInfo("demo-client")));
        when(oauthClientManagementClient.create(any(OauthClientCreateRequest.class)))
                .thenReturn(R.ok(OauthClientSecretRotateResult.builder()
                        .clientId("demo-client")
                        .plainClientSecret("plain-secret")
                        .maskedClientSecret("pla***ret")
                        .version(1)
                        .build()));

        GovernanceTargetAdapter.AdapterPublishResult result = adapter.publish(new GovernanceTargetAdapter.TargetConfig(
                resourceRef("demo-client"),
                """
                        {"operation":"CREATE","clientId":"demo-client","scope":"all","authorizedGrantTypes":"password","status":true}
                        """, null), new GovernanceTargetAdapter.PublishContext("REL-001", "operator", "publish", null));

        assertThat(result.success()).isTrue();
        assertThat(result.afterContent()).contains("demo-client").doesNotContain("plain-secret");
        assertThat(result.effectiveHint()).contains("pla***ret").doesNotContain("plain-secret");
        ArgumentCaptor<OauthClientCreateRequest> captor = ArgumentCaptor.forClass(OauthClientCreateRequest.class);
        verify(oauthClientManagementClient).create(captor.capture());
        assertThat(captor.getValue().getClientSecret()).isNull();
    }

    @Test
    void publishRotateSecretShouldNotSendPlainSecret() {
        when(oauthClientManagementClient.query(any(OauthClientQueryRequest.class)))
                .thenReturn(R.ok(clientInfo("demo-client")))
                .thenReturn(R.ok(clientInfo("demo-client")));
        when(oauthClientManagementClient.rotateSecret(any(OauthClientRotateSecretRequest.class)))
                .thenReturn(R.ok(OauthClientSecretRotateResult.builder()
                        .clientId("demo-client")
                        .plainClientSecret("new-secret")
                        .maskedClientSecret("new***ret")
                        .version(2)
                        .build()));

        GovernanceTargetAdapter.AdapterPublishResult result = adapter.publish(new GovernanceTargetAdapter.TargetConfig(
                resourceRef("demo-client"),
                """
                        {"operation":"ROTATE_SECRET","clientId":"demo-client","version":1}
                        """, null), new GovernanceTargetAdapter.PublishContext("REL-002", "operator", "rotate", null));

        assertThat(result.success()).isTrue();
        assertThat(result.afterContent()).doesNotContain("new-secret");
        ArgumentCaptor<OauthClientRotateSecretRequest> captor = ArgumentCaptor.forClass(OauthClientRotateSecretRequest.class);
        verify(oauthClientManagementClient).rotateSecret(captor.capture());
        assertThat(captor.getValue().getNewClientSecret()).isNull();
    }

    private OauthClientInfo clientInfo(String clientId) {
        return OauthClientInfo.builder()
                .clientId(clientId)
                .scope("all")
                .authorizedGrantTypes("password")
                .status(true)
                .version(1)
                .build();
    }

    private GovernanceTargetAdapter.ResourceRef resourceRef(String clientId) {
        return new GovernanceTargetAdapter.ResourceRef(adapter.domain(), "OAUTH_CLIENT", clientId, clientId);
    }
}
