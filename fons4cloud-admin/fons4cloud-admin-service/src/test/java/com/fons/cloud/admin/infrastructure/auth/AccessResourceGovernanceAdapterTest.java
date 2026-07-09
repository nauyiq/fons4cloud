package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.auth.core.AuthorizationResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 授权资源治理适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class AccessResourceGovernanceAdapterTest {

    @Mock
    private AuthorizationResourceRepository repository;

    private AccessResourceGovernanceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccessResourceGovernanceAdapter(repository);
    }

    @Test
    void validateShouldPassForAuthorizationResourceAndUris() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                """
                        {"authorizationResources":[{"id":"GET_/admin/audits","authorities":["audits:view"]}],"ignoredAccessTokenUris":["/public/login"],"identifierTokenUris":["/orders"]}
                        """, null));

        assertThat(result.getPassed()).isTrue();
        assertThat(result.getNormalizedContentHash()).isNotBlank();
    }

    @Test
    void validateShouldReturnFieldErrorsForInvalidResource() {
        GovernanceValidateResult result = adapter.validate(new GovernanceTargetAdapter.TargetConfig(resourceRef(),
                """
                        {"authorizationResources":[{"id":"BAD_/admin/audits","authorities":[]},{"id":"GET_/admin/audits","authorities":[""]},{"id":"GET_/admin/audits","authorities":["audits:view"]}],"ignoredAccessTokenUris":["bad uri"]}
                        """, null));

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getErrors()).extracting("code")
                .contains("ACCESS_RESOURCE_ID_INVALID", "ACCESS_AUTHORITIES_EMPTY",
                        "ACCESS_AUTHORITIES_BLANK", "ACCESS_RESOURCE_DUPLICATED", "ACCESS_URI_INVALID");
    }

    @Test
    void loadCurrentShouldReadRepositoryAndReturnHash() {
        when(repository.getAuthorizationResources()).thenReturn(Map.of("GET_/admin/audits", Set.of("audits:view")));
        when(repository.getIgnoredAccessTokenUri()).thenReturn(Set.of("/public/login"));
        when(repository.getIdentifierTokenUri()).thenReturn(Set.of("/orders"));

        GovernanceTargetAdapter.CurrentConfig currentConfig = adapter.loadCurrent(resourceRef());

        assertThat(currentConfig.content()).contains("GET_/admin/audits", "audits:view", "/public/login", "/orders");
        assertThat(currentConfig.contentHash()).isNotBlank();
        assertThat(currentConfig.targetRef()).isEqualTo("auth:authorization-resource");
    }

    @Test
    void publishShouldReplaceRepositoryResourcesAndUris() {
        when(repository.getAuthorizationResources())
                .thenReturn(Map.of("GET_/old", Set.of("old:view")))
                .thenReturn(Map.of("POST_/admin/changes", Set.of("changes:edit")));
        when(repository.getIgnoredAccessTokenUri())
                .thenReturn(Set.of("/old/public"))
                .thenReturn(Set.of("/admin/auth/login"));
        when(repository.getIdentifierTokenUri())
                .thenReturn(Set.of("/old/idempotent"))
                .thenReturn(Set.of("/admin/changes"));

        GovernanceTargetAdapter.AdapterPublishResult result = adapter.publish(new GovernanceTargetAdapter.TargetConfig(
                resourceRef(),
                """
                        {"authorizationResources":[{"id":"POST_/admin/changes","authorities":["changes:edit"]}],"ignoredAccessTokenUris":["/admin/auth/login"],"identifierTokenUris":["/admin/changes"]}
                        """, null), new GovernanceTargetAdapter.PublishContext("REL-001", "operator", "publish", null));

        assertThat(result.success()).isTrue();
        assertThat(result.beforeContent()).contains("GET_/old", "/old/public", "/old/idempotent");
        assertThat(result.afterContent()).contains("POST_/admin/changes", "/admin/auth/login", "/admin/changes");

        ArgumentCaptor<Map<String, Set<String>>> resourcesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(repository).replaceAuthorizationResources(resourcesCaptor.capture());
        assertThat(resourcesCaptor.getValue()).containsEntry("POST_/admin/changes", Set.of("changes:edit"));
        verify(repository).replaceIgnoredAccessTokenUri(Set.of("/admin/auth/login"));
        verify(repository).replaceIdentifierTokenUri(Set.of("/admin/changes"));
    }

    private GovernanceTargetAdapter.ResourceRef resourceRef() {
        return new GovernanceTargetAdapter.ResourceRef(adapter.domain(), "AUTH_RESOURCE",
                "authorization-resource", "auth:authorization-resource");
    }
}
