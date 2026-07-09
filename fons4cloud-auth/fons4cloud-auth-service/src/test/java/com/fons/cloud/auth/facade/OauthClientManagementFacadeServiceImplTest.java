package com.fons.cloud.auth.facade;

import com.fons.cloud.auth.domain.entity.OauthClient;
import com.fons.cloud.auth.domain.service.SysOauthClientDomainService;
import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientModifyRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OAuth Client 管理 RPC 的契约级单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OauthClientManagementFacadeServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SysOauthClientDomainService oauthClientDomainService;

    @InjectMocks
    private OauthClientManagementFacadeServiceImpl facadeService;

    @Test
    void createShouldEncryptSecretReturnPlainSecretOnceAndEvictCache() {
        OauthClientCreateRequest request = OauthClientCreateRequest.builder()
                .clientId("client-app")
                .clientSecret("plain-secret")
                .scope("read")
                .authorizedGrantTypes("client_credentials")
                .status(Boolean.TRUE)
                .build();
        when(oauthClientDomainService.getById("client-app")).thenReturn(null);
        when(passwordEncoder.encode("plain-secret")).thenReturn("{bcrypt}encoded-secret");
        when(oauthClientDomainService.save(any(OauthClient.class))).thenAnswer(invocation -> {
            OauthClient client = invocation.getArgument(0);
            client.setVersion(1);
            return true;
        });

        R<OauthClientSecretRotateResult> response = facadeService.create(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getPlainClientSecret()).isEqualTo("plain-secret");
        assertThat(response.getData().getMaskedClientSecret()).isEqualTo("plai******cret");

        ArgumentCaptor<OauthClient> clientCaptor = ArgumentCaptor.forClass(OauthClient.class);
        verify(oauthClientDomainService).save(clientCaptor.capture());
        OauthClient savedClient = clientCaptor.getValue();
        assertThat(savedClient.getClientId()).isEqualTo("client-app");
        assertThat(savedClient.getClientSecret()).isEqualTo("{bcrypt}encoded-secret");
        assertThat(savedClient.getScope()).isEqualTo("read");
        verify(oauthClientDomainService).evictClientCache("client-app");
    }

    @Test
    void updateShouldKeepOriginalValueWhenRequestFieldIsNullAndEvictCache() {
        OauthClient exists = new OauthClient();
        exists.setClientId("client-app");
        exists.setResourceIds("resource-a");
        exists.setScope("read");
        exists.setStatus(Boolean.TRUE);
        exists.setVersion(2);

        OauthClientModifyRequest request = OauthClientModifyRequest.builder()
                .clientId("client-app")
                .status(Boolean.FALSE)
                .version(3)
                .build();
        when(oauthClientDomainService.getById("client-app")).thenReturn(exists);
        when(oauthClientDomainService.updateById(exists)).thenReturn(true);

        R<OauthClientInfo> response = facadeService.update(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getScope()).isEqualTo("read");
        assertThat(response.getData().getStatus()).isFalse();
        assertThat(exists.getResourceIds()).isEqualTo("resource-a");
        assertThat(exists.getVersion()).isEqualTo(3);
        verify(oauthClientDomainService).evictClientCache("client-app");
    }
}
