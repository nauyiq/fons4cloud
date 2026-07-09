package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.request.RefreshTokenRequest;
import com.fons.cloud.auth.response.TokenInfo;
import com.fons.cloud.common.result.R;

/**
 * admin 认证 RPC 客户端端口，隔离 auth-service Dubbo 调用细节。
 */
public interface AdminAuthenticationClient {

    /**
     * 调用认证服务完成账号认证并获取 Token。
     *
     * @param request 认证请求
     * @return 认证服务 Token 响应
     */
    R<TokenInfo> authenticate(AuthenticateRequest request);

    /**
     * 调用认证服务刷新 Token。
     *
     * @param request 刷新 Token 请求
     * @return 认证服务 Token 响应
     */
    R<TokenInfo> refreshToken(RefreshTokenRequest request);

    /**
     * 调用认证服务吊销 Token。
     *
     * @param accessToken 访问令牌
     * @return 是否吊销成功
     */
    R<Boolean> revokeToken(String accessToken);
}
