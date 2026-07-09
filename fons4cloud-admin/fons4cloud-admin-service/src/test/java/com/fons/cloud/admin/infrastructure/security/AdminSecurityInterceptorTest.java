package com.fons.cloud.admin.infrastructure.security;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSON;
import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.auth.api.support.DefaultAuthUser;
import com.fons.cloud.auth.common.AuthUserHeaderConstants;
import com.fons.cloud.auth.common.UserRole;
import com.fons.cloud.common.result.ResultCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * admin MVC 鉴权拦截器测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminSecurityInterceptorTest {

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    private AdminSecurityInterceptor adminSecurityInterceptor;

    @BeforeEach
    void setUp() {
        adminSecurityInterceptor = new AdminSecurityInterceptor(adminAuthorizationService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        com.fons.cloud.auth.utils.AuthUtils.removeUser();
    }

    @Test
    void preHandleShouldRejectMissingAuthUserHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminSecurityInterceptor.preHandle(request, response, handler("viewUsers"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(ResultCode.INVALID_ACCESS_TOKEN.getCode());
    }

    @Test
    void preHandleShouldRejectUnconfiguredAdminResource() throws Exception {
        when(adminAuthorizationService.authorize(any(), anyCollection()))
                .thenReturn(AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_PERMISSION_DENIED));
        MockHttpServletRequest request = requestWithAuthUser();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminSecurityInterceptor.preHandle(request, response, handler("unconfigured"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains(AdminResultCode.ADMIN_PERMISSION_DENIED.getCode());
    }

    @Test
    void preHandleShouldRejectDeniedAdminUser() throws Exception {
        when(adminAuthorizationService.authorize(any(), anyCollection()))
                .thenReturn(AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_USER_NOT_BOUND));
        MockHttpServletRequest request = requestWithAuthUser();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminSecurityInterceptor.preHandle(request, response, handler("viewUsers"));

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains(AdminResultCode.ADMIN_USER_NOT_BOUND.getCode());
    }

    @Test
    void preHandleShouldAllowPermittedAdminUser() throws Exception {
        when(adminAuthorizationService.authorize(any(), anyCollection())).thenReturn(AdminAuthorizationDecision.allow());

        boolean allowed = adminSecurityInterceptor.preHandle(requestWithAuthUser(), new MockHttpServletResponse(), handler("viewUsers"));

        assertThat(allowed).isTrue();
    }

    private MockHttpServletRequest requestWithAuthUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        DefaultAuthUser authUser = new DefaultAuthUser(1L, "root", "root@example.com", "13800000000", UserRole.ADMIN, List.of());
        request.addHeader(AuthUserHeaderConstants.AUTH_USER, Base64.encode(JSON.toJSONString(authUser)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        Method method = TestAdminController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new TestAdminController(), method);
    }

    private static class TestAdminController {

        @AdminPermission(authorities = AdminPermissionCodes.ACCESS_VIEW)
        void viewUsers() {
        }

        void unconfigured() {
        }
    }
}
