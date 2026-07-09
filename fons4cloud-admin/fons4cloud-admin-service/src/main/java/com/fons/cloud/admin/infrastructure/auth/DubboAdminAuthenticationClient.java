package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.request.RefreshTokenRequest;
import com.fons.cloud.auth.response.TokenInfo;
import com.fons.cloud.auth.service.AccountAuthenticationFacadeService;
import com.fons.cloud.common.result.R;
import com.fons.cloud.dubbo.DubboConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 基于 Dubbo 的认证服务 RPC 客户端。
 */
@Component
public class DubboAdminAuthenticationClient implements AdminAuthenticationClient {

    @DubboReference(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)
    private AccountAuthenticationFacadeService accountAuthenticationFacadeService;

    @Override
    public R<TokenInfo> authenticate(AuthenticateRequest request) {
        return accountAuthenticationFacadeService.authenticate(request);
    }

    @Override
    public R<TokenInfo> refreshToken(RefreshTokenRequest request) {
        return accountAuthenticationFacadeService.refreshToken(request);
    }

    @Override
    public R<Boolean> revokeToken(String accessToken) {
        return accountAuthenticationFacadeService.revokeToken(accessToken);
    }
}
