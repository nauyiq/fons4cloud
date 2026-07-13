package com.fons.cloud.admin.interfaces.rest;

import cn.hutool.core.util.StrUtil;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.application.AdminAuthApplicationService;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthProperties;
import com.fons.cloud.admin.interfaces.rest.api.model.AdminAccessTokenResponse;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import com.fons.cloud.web.annotation.BsWebAdvice;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * admin 认证 REST API。
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private static final String BEARER_PREFIX = "Bearer";

    private final AdminAuthApplicationService adminAuthApplicationService;
    private final AdminAuthProperties adminAuthProperties;

    /**
     * admin 登录入口；内部通过 auth-service RPC 获取 Token。
     *
     * @param request 登录请求
     * @return admin Token 响应
     */
    @PostMapping("/login")
    @BsWebAdvice(requiredToken = false)
    public R<AdminAccessTokenResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                             HttpServletResponse response) {
        return exposeBrowserSession(adminAuthApplicationService.login(request), response);
    }

    /**
     * admin 刷新 Token 入口；客户端不需要也不能传入控制面客户端密钥。
     *
     * @param refreshToken HttpOnly Cookie 中的 Refresh Token
     * @return 新的 admin Token 响应
     */
    @PostMapping("/refresh-token")
    @BsWebAdvice(requiredToken = false)
    public R<AdminAccessTokenResponse> refreshToken(
            HttpServletRequest servletRequest,
            HttpServletResponse response) {
        String refreshToken = resolveRefreshCookie(servletRequest);
        if (StrUtil.isBlank(refreshToken)) {
            clearRefreshCookie(response);
            return R.failed(AdminResultCode.ADMIN_REFRESH_COOKIE_INVALID);
        }
        AdminRefreshTokenRequest request = AdminRefreshTokenRequest.builder().refreshToken(refreshToken).build();
        return exposeBrowserSession(adminAuthApplicationService.refreshToken(request), response);
    }

    /**
     * admin 退出登录入口；只向认证服务传递访问令牌，不记录令牌明文。
     *
     * @param authorization Authorization 请求头
     * @return 是否退出成功
    */
    @DeleteMapping("/logout")
    @AuthenticationResource(authorities = "ADMIN")
    public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                             HttpServletResponse response) {
        clearRefreshCookie(response);
        String accessToken = resolveBearerToken(authorization);
        if (StrUtil.isBlank(accessToken)) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        return adminAuthApplicationService.logout(accessToken);
    }

    private String resolveBearerToken(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            return StrUtil.EMPTY;
        }
        return authorization.replace(BEARER_PREFIX, StrUtil.EMPTY).trim();
    }

    private R<AdminAccessTokenResponse> exposeBrowserSession(R<AdminTokenResponse> result,
                                                              HttpServletResponse response) {
        if (result == null || !result.isSuccess() || result.getData() == null) {
            clearRefreshCookie(response);
            return result == null
                    ? R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED)
                    : R.failed(result.getCode(), result.getMessage());
        }
        AdminTokenResponse token = result.getData();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(token.getRefreshToken(),
                adminAuthProperties.getRefreshCookieMaxAgeSeconds()).toString());
        return R.ok(new AdminAccessTokenResponse(token.getAccessToken(), token.getTokenType(), token.getExpiresIn(),
                token.getScopes(), token.getUserId(), token.getUsername()));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(StrUtil.EMPTY, 0L).toString());
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(adminAuthProperties.getRefreshCookieName(), value)
                .httpOnly(true)
                .secure(adminAuthProperties.isRefreshCookieSecure())
                .sameSite(adminAuthProperties.getRefreshCookieSameSite())
                .path(adminAuthProperties.getRefreshCookiePath())
                .maxAge(maxAgeSeconds)
                .build();
    }

    private String resolveRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return StrUtil.EMPTY;
        }
        for (Cookie cookie : request.getCookies()) {
            if (adminAuthProperties.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return StrUtil.EMPTY;
    }
}
