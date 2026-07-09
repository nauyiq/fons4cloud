package com.fons.cloud.admin.interfaces.rest;

import cn.hutool.core.util.StrUtil;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.application.AdminAuthApplicationService;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import com.fons.cloud.web.annotation.BsWebAdvice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    /**
     * admin 登录入口；内部通过 auth-service RPC 获取 Token。
     *
     * @param request 登录请求
     * @return admin Token 响应
     */
    @PostMapping("/login")
    @BsWebAdvice(requiredToken = false)
    public R<AdminTokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthApplicationService.login(request);
    }

    /**
     * admin 刷新 Token 入口；客户端不需要也不能传入控制面客户端密钥。
     *
     * @param request 刷新 Token 请求
     * @return 新的 admin Token 响应
     */
    @PostMapping("/refresh-token")
    @BsWebAdvice(requiredToken = false)
    public R<AdminTokenResponse> refreshToken(@Valid @RequestBody AdminRefreshTokenRequest request) {
        return adminAuthApplicationService.refreshToken(request);
    }

    /**
     * admin 退出登录入口；只向认证服务传递访问令牌，不记录令牌明文。
     *
     * @param authorization Authorization 请求头
     * @return 是否退出成功
    */
    @DeleteMapping("/logout")
    @AuthenticationResource(authorities = "ADMIN")
    public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
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
}
