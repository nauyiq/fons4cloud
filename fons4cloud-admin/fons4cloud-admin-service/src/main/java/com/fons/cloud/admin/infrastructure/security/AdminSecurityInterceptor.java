package com.fons.cloud.admin.infrastructure.security;

import com.alibaba.fastjson2.JSON;
import com.fons.cloud.auth.api.AuthUser;
import com.fons.cloud.auth.common.AuthException;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import com.fons.cloud.common.result.Result;
import com.fons.cloud.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * admin MVC 鉴权拦截器，确保 `/admin/**` 先完成登录态、管理员绑定和 RBAC 权限校验。
 */
@Component
@RequiredArgsConstructor
public class AdminSecurityInterceptor implements HandlerInterceptor {

    private final AdminAuthorizationService adminAuthorizationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        AuthUser authUser;
        try {
            authUser = AuthUtils.getCurrentUser();
        } catch (AuthException | IllegalArgumentException cause) {
            writeDenied(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.INVALID_ACCESS_TOKEN);
            return false;
        }
        AdminPermission permission = handlerMethod.getMethodAnnotation(AdminPermission.class);
        Collection<String> requiredPermissions = requiredPermissions(handlerMethod);
        AdminAuthorizationDecision decision = permission != null && requiredPermissions.isEmpty()
                ? adminAuthorizationService.authorizeAdmin(authUser)
                : adminAuthorizationService.authorize(authUser, requiredPermissions);
        if (decision.isAllowed()) {
            return true;
        }
        writeDenied(response, HttpServletResponse.SC_FORBIDDEN, decision.getDeniedResult());
        return false;
    }

    private Collection<String> requiredPermissions(HandlerMethod handlerMethod) {
        AdminPermission annotation = handlerMethod.getMethodAnnotation(AdminPermission.class);
        if (annotation == null) {
            // 未声明 admin 内部权限点的接口默认拒绝，避免遗漏 RBAC 声明。
            return Collections.emptySet();
        }
        return Arrays.asList(annotation.authorities());
    }

    private void writeDenied(HttpServletResponse response, int status, Result result) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSON.toJSONString(R.failed(result)));
    }
}
