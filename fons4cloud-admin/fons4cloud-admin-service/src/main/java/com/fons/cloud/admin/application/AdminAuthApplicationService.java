package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.admin.domain.mapper.AdminUserMapper;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthProperties;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthenticationClient;
import com.fons.cloud.admin.infrastructure.converter.AdminAuthConverter;
import com.fons.cloud.auth.constants.GrantType;
import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.request.RefreshTokenRequest;
import com.fons.cloud.auth.response.TokenInfo;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * admin 认证应用服务，负责通过认证服务 RPC 获取、刷新和吊销控制面 Token。
 */
@Service
@RequiredArgsConstructor
public class AdminAuthApplicationService {

    private final AdminAuthProperties adminAuthProperties;
    private final AdminAuthenticationClient authenticationClient;
    private final AdminUserMapper adminUserMapper;

    /**
     * 使用服务端配置的控制面客户端调用认证服务 RPC，并在返回 Token 后校验 admin 管理员绑定。
     *
     * @param request admin 登录请求
     * @return admin Token 响应
     */
    public R<AdminTokenResponse> login(AdminLoginRequest request) {
        R<TokenInfo> tokenResult;
        try {
            tokenResult = authenticationClient.authenticate(buildAuthenticateRequest(request));
        } catch (RuntimeException ex) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        if (rpcFailed(tokenResult)) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }

        TokenInfo tokenInfo = tokenResult.getData();
        AdminUser adminUser = findAdminUser(tokenInfo.getUserId());
        if (adminUser == null) {
            revokeQuietly(tokenInfo.getAccessToken());
            return R.failed(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        if (!AdminUserStatus.ACTIVE.name().equals(adminUser.getStatus())) {
            revokeQuietly(tokenInfo.getAccessToken());
            return R.failed(AdminResultCode.ADMIN_USER_DISABLED);
        }

        return R.ok(AdminAuthConverter.CONVERTER.mapToAdminTokenResponse(tokenInfo, adminUser));
    }

    /**
     * 使用服务端配置的控制面客户端刷新 Token，并确认管理员仍然处于启用状态。
     *
     * @param request 刷新 Token 请求
     * @return 新的 admin Token 响应
     */
    public R<AdminTokenResponse> refreshToken(AdminRefreshTokenRequest request) {
        R<TokenInfo> tokenResult;
        try {
            tokenResult = authenticationClient.refreshToken(RefreshTokenRequest.builder()
                    .clientId(adminAuthProperties.getClientId())
                    .clientSecret(adminAuthProperties.getClientSecret())
                    .refreshToken(request.getRefreshToken())
                    .build());
        } catch (RuntimeException ex) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        if (rpcFailed(tokenResult)) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }

        TokenInfo tokenInfo = tokenResult.getData();
        AdminUser adminUser = findAdminUser(tokenInfo.getUserId());
        if (adminUser == null) {
            return R.failed(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        if (!AdminUserStatus.ACTIVE.name().equals(adminUser.getStatus())) {
            revokeQuietly(tokenInfo.getAccessToken());
            return R.failed(AdminResultCode.ADMIN_USER_DISABLED);
        }

        return R.ok(AdminAuthConverter.CONVERTER.mapToAdminTokenResponse(tokenInfo, adminUser));
    }

    /**
     * 通过认证服务 RPC 吊销访问令牌。
     *
     * @param accessToken 访问令牌
     * @return 是否退出成功
     */
    public R<Boolean> logout(String accessToken) {
        R<Boolean> revokeResult;
        try {
            revokeResult = authenticationClient.revokeToken(accessToken);
        } catch (RuntimeException ex) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        if (rpcFailed(revokeResult)) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        return R.ok(Boolean.TRUE.equals(revokeResult.getData()));
    }

    private AuthenticateRequest buildAuthenticateRequest(AdminLoginRequest request) {
        return AuthenticateRequest.builder()
                .clientId(adminAuthProperties.getClientId())
                .clientSecret(adminAuthProperties.getClientSecret())
                .accessAccount(request.getAccessAccount())
                .accessSecret(request.getAccessSecret())
                .grantType(GrantType.valueOf(request.getGrantType().toUpperCase()))
                .scopes(request.getScopes())
                .build();
    }

    private AdminUser findAdminUser(Long accountId) {
        return adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getAccountId, accountId));
    }

    private void revokeQuietly(String accessToken) {
        if (accessToken != null) {
            try {
                authenticationClient.revokeToken(accessToken);
            } catch (RuntimeException ignored) {
                // Token cleanup must not hide the original admin login decision.
            }
        }
    }

    private boolean rpcFailed(R<?> rpcResult) {
        return rpcResult == null || !rpcResult.isSuccess() || Objects.isNull(rpcResult.getData());
    }
}
