package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.admin.domain.mapper.AdminUserMapper;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthProperties;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthenticationClient;
import com.fons.cloud.auth.constants.GrantType;
import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.request.RefreshTokenRequest;
import com.fons.cloud.auth.response.TokenInfo;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * admin 认证应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthApplicationServiceTest {

    @Mock
    private AdminAuthenticationClient authenticationClient;

    @Mock
    private AdminUserMapper adminUserMapper;

    private AdminAuthApplicationService adminAuthApplicationService;

    @BeforeEach
    void setUp() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setClientId("sys-admin");
        properties.setClientSecret("server-secret");
        adminAuthApplicationService = new AdminAuthApplicationService(properties, authenticationClient, adminUserMapper);
    }

    @Test
    void loginShouldUseServerAdminClientAndReturnAdminUserToken() {
        AdminLoginRequest request = AdminLoginRequest.builder()
                .accessAccount("root")
                .accessSecret("password")
                .grantType("PASSWORD")
                .scopes(Set.of("all"))
                .build();
        when(authenticationClient.authenticate(any(AuthenticateRequest.class)))
                .thenReturn(R.ok(tokenInfo(100L, "root", "access-token", "refresh-token")));
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(adminUser(10L, 100L, "root", AdminUserStatus.ACTIVE.name()));

        R<AdminTokenResponse> response = adminAuthApplicationService.login(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getUserId()).isEqualTo(10L);
        assertThat(response.getData().getUsername()).isEqualTo("root");
        assertThat(response.getData().getAccessToken()).isEqualTo("access-token");
        verify(authenticationClient, never()).revokeToken(any());

        ArgumentCaptor<AuthenticateRequest> requestCaptor = ArgumentCaptor.forClass(AuthenticateRequest.class);
        verify(authenticationClient).authenticate(requestCaptor.capture());
        AuthenticateRequest rpcRequest = requestCaptor.getValue();
        assertThat(rpcRequest.getClientId()).isEqualTo("sys-admin");
        assertThat(rpcRequest.getClientSecret()).isEqualTo("server-secret");
        assertThat(rpcRequest.getGrantType()).isEqualTo(GrantType.PASSWORD);
        assertThat(rpcRequest.getAccessAccount()).isEqualTo("root");
        assertThat(rpcRequest.getAccessSecret()).isEqualTo("password");
    }

    @Test
    void loginShouldRevokeTokenWhenAdminUserNotBound() {
        when(authenticationClient.authenticate(any(AuthenticateRequest.class)))
                .thenReturn(R.ok(tokenInfo(100L, "root", "access-token", "refresh-token")));
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        R<AdminTokenResponse> response = adminAuthApplicationService.login(loginRequest());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_USER_NOT_BOUND.getCode());
        verify(authenticationClient).revokeToken("access-token");
    }

    @Test
    void loginShouldRevokeTokenWhenAdminUserDisabled() {
        when(authenticationClient.authenticate(any(AuthenticateRequest.class)))
                .thenReturn(R.ok(tokenInfo(100L, "root", "access-token", "refresh-token")));
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(adminUser(10L, 100L, "root", AdminUserStatus.DISABLED.name()));

        R<AdminTokenResponse> response = adminAuthApplicationService.login(loginRequest());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_USER_DISABLED.getCode());
        verify(authenticationClient).revokeToken("access-token");
    }

    @Test
    void loginShouldReturnAuthRpcFailedWhenAuthenticateThrowsException() {
        when(authenticationClient.authenticate(any(AuthenticateRequest.class)))
                .thenThrow(new RuntimeException("rpc unavailable"));

        R<AdminTokenResponse> response = adminAuthApplicationService.login(loginRequest());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_AUTH_RPC_FAILED.getCode());
        verifyNoInteractions(adminUserMapper);
    }

    @Test
    void refreshTokenShouldUseServerAdminClientAndCheckAdminUser() {
        when(authenticationClient.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(R.ok(tokenInfo(100L, "root", "new-access-token", "new-refresh-token")));
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(adminUser(10L, 100L, "root", AdminUserStatus.ACTIVE.name()));

        R<AdminTokenResponse> response = adminAuthApplicationService.refreshToken(
                AdminRefreshTokenRequest.builder().refreshToken("old-refresh-token").build());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getAccessToken()).isEqualTo("new-access-token");

        ArgumentCaptor<RefreshTokenRequest> requestCaptor = ArgumentCaptor.forClass(RefreshTokenRequest.class);
        verify(authenticationClient).refreshToken(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getClientId()).isEqualTo("sys-admin");
        assertThat(requestCaptor.getValue().getClientSecret()).isEqualTo("server-secret");
        assertThat(requestCaptor.getValue().getRefreshToken()).isEqualTo("old-refresh-token");
    }

    @Test
    void refreshTokenShouldReturnAuthRpcFailedWhenRefreshThrowsException() {
        when(authenticationClient.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new RuntimeException("rpc unavailable"));

        R<AdminTokenResponse> response = adminAuthApplicationService.refreshToken(
                AdminRefreshTokenRequest.builder().refreshToken("old-refresh-token").build());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_AUTH_RPC_FAILED.getCode());
        verifyNoInteractions(adminUserMapper);
    }

    @Test
    void logoutShouldRevokeAccessTokenByRpc() {
        when(authenticationClient.revokeToken("access-token")).thenReturn(R.ok(Boolean.TRUE));

        R<Boolean> response = adminAuthApplicationService.logout("access-token");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isTrue();
        verify(authenticationClient).revokeToken("access-token");
    }

    @Test
    void logoutShouldReturnAuthRpcFailedWhenRevokeThrowsException() {
        when(authenticationClient.revokeToken("access-token")).thenThrow(new RuntimeException("rpc unavailable"));

        R<Boolean> response = adminAuthApplicationService.logout("access-token");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_AUTH_RPC_FAILED.getCode());
    }

    private AdminLoginRequest loginRequest() {
        return AdminLoginRequest.builder()
                .accessAccount("root")
                .accessSecret("password")
                .grantType("PASSWORD")
                .build();
    }

    private TokenInfo tokenInfo(Long accountId, String username, String accessToken, String refreshToken) {
        return TokenInfo.builder()
                .userId(accountId)
                .username(username)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .scopes(Set.of("all"))
                .build();
    }

    private AdminUser adminUser(Long id, Long accountId, String username, String status) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(id);
        adminUser.setAccountId(accountId);
        adminUser.setUsername(username);
        adminUser.setStatus(status);
        return adminUser;
    }
}
