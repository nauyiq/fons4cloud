package com.fons.cloud.admin.infrastructure.security;

import com.fons.cloud.admin.interfaces.rest.AccessGovernanceController;
import com.fons.cloud.admin.interfaces.rest.AdminAuthController;
import com.fons.cloud.admin.interfaces.rest.AuditController;
import com.fons.cloud.admin.interfaces.rest.ChangeController;
import com.fons.cloud.admin.interfaces.rest.ClientGovernanceController;
import com.fons.cloud.admin.interfaces.rest.GatewayGovernanceController;
import com.fons.cloud.admin.interfaces.rest.ObservabilityController;
import com.fons.cloud.admin.interfaces.rest.ServiceGovernanceController;
import com.fons.cloud.admin.interfaces.rest.TrafficGovernanceController;
import com.fons.cloud.admin.interfaces.rest.api.AdminSessionController;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.web.annotation.BsWebAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * admin REST Controller 权限注解边界测试。
 */
class AdminResourceAnnotationBoundaryTest {

    private static final String GATEWAY_ADMIN_AUTHORITY = "ADMIN";

    @Test
    void protectedAdminRestEndpointsShouldDeclareGatewayAndInternalPermissions() {
        List<Method> protectedMethods = controllerMethods().stream()
                .filter(this::isMappedEndpoint)
                .filter(method -> !isAnonymousEndpoint(method))
                .filter(method -> !isAuthControllerMethod(method))
                .toList();

        assertThat(protectedMethods).isNotEmpty();
        assertThat(protectedMethods).allSatisfy(method -> {
            AuthenticationResource gatewayResource = AnnotationUtils.findAnnotation(method, AuthenticationResource.class);
            AdminPermission adminPermission = AnnotationUtils.findAnnotation(method, AdminPermission.class);

            assertThat(gatewayResource)
                    .as("%s should declare gateway ADMIN authority", method)
                    .isNotNull();
            assertThat(gatewayResource.authorities())
                    .as("%s should reuse framework resource authorization", method)
                    .containsExactly(GATEWAY_ADMIN_AUTHORITY);
            assertThat(adminPermission)
                    .as("%s should declare admin internal RBAC permission", method)
                    .isNotNull();
            assertThat(adminPermission.authorities())
                    .as("%s should not use gateway ADMIN as internal RBAC permission", method)
                    .doesNotContain(GATEWAY_ADMIN_AUTHORITY);
        });
    }

    @Test
    void tokenProtectedAdminAuthEndpointsShouldDeclareGatewayPermissionOnly() {
        List<Method> tokenProtectedAuthMethods = Arrays.stream(AdminAuthController.class.getDeclaredMethods())
                .filter(this::isMappedEndpoint)
                .filter(method -> !isAnonymousEndpoint(method))
                .toList();

        assertThat(tokenProtectedAuthMethods).isNotEmpty();
        assertThat(tokenProtectedAuthMethods).allSatisfy(method -> {
            AuthenticationResource gatewayResource = AnnotationUtils.findAnnotation(method, AuthenticationResource.class);
            AdminPermission adminPermission = AnnotationUtils.findAnnotation(method, AdminPermission.class);

            assertThat(gatewayResource)
                    .as("%s should declare gateway ADMIN authority", method)
                    .isNotNull();
            assertThat(gatewayResource.authorities())
                    .as("%s should reuse framework resource authorization", method)
                    .containsExactly(GATEWAY_ADMIN_AUTHORITY);
            assertThat(adminPermission)
                    .as("%s should not declare internal RBAC permission because auth endpoints are excluded from admin RBAC", method)
                    .isNull();
        });
    }

    private List<Method> controllerMethods() {
        return List.of(
                AccessGovernanceController.class,
                AuditController.class,
                ChangeController.class,
                ClientGovernanceController.class,
                GatewayGovernanceController.class,
                ObservabilityController.class,
                ServiceGovernanceController.class,
                TrafficGovernanceController.class,
                AdminSessionController.class,
                AdminAuthController.class
        ).stream().flatMap(type -> Arrays.stream(type.getDeclaredMethods())).toList();
    }

    private boolean isMappedEndpoint(Method method) {
        return AnnotationUtils.findAnnotation(method, GetMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PostMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PutMapping.class) != null
                || AnnotationUtils.findAnnotation(method, DeleteMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PatchMapping.class) != null
                || AnnotationUtils.findAnnotation(method, RequestMapping.class) != null;
    }

    private boolean isAnonymousEndpoint(Method method) {
        BsWebAdvice bsWebAdvice = AnnotationUtils.findAnnotation(method, BsWebAdvice.class);
        return bsWebAdvice != null && !bsWebAdvice.requiredToken();
    }

    private boolean isAuthControllerMethod(Method method) {
        return method.getDeclaringClass().equals(AdminAuthController.class);
    }
}
