package com.fons.cloud.gateway.server.auth;

import com.fons.cloud.auth.api.AuthPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * gateway 认证授权管理器测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationManagerTest {

    @Mock
    private AuthPermissionService authPermissionService;

    @Test
    void checkShouldPermitRuntimeBusinessWhiteUriWithoutAuthentication() {
        AuthorizationManager authorizationManager = new AuthorizationManager(authPermissionService);
        AuthorizationContext context = context(HttpMethod.POST, "/admin/auth/login");
        when(authPermissionService.isPermitAnonymousRequest(argThat(request ->
                "/admin/auth/login".equals(request.requestUri()) && request.authorities() == null)))
                .thenReturn(true);

        AuthorizationDecision decision = authorizationManager.check(Mono.empty(), context).block();

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void checkShouldDelegateProtectedRequestToFrameworkResourceAuthorization() {
        AuthorizationManager authorizationManager = new AuthorizationManager(authPermissionService);
        AuthorizationContext context = context(HttpMethod.GET, "/admin/services");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "pwd", "USER");
        authentication.setAuthenticated(true);
        doReturn(false).when(authPermissionService).isPermitAnonymousRequest(any());
        doReturn(false).when(authPermissionService).isPermitRequest(argThat(request ->
                "/admin/services".equals(request.requestUri()) && request.authorities().contains("USER")));

        AuthorizationDecision decision = authorizationManager.check(Mono.just(authentication), context).block();

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        verify(authPermissionService).isPermitAnonymousRequest(any());
        verify(authPermissionService).isPermitRequest(any());
    }

    @Test
    void checkShouldPermitProtectedRequestWhenFrameworkResourceAuthorizationAllows() {
        AuthorizationManager authorizationManager = new AuthorizationManager(authPermissionService);
        AuthorizationContext context = context(HttpMethod.GET, "/admin/services");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "pwd", "ADMIN");
        authentication.setAuthenticated(true);
        doReturn(false).when(authPermissionService).isPermitAnonymousRequest(any());
        doReturn(true).when(authPermissionService).isPermitRequest(argThat(request ->
                "/admin/services".equals(request.requestUri()) && request.authorities().contains("ADMIN")));

        AuthorizationDecision decision = authorizationManager.check(Mono.just(authentication), context).block();

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    private AuthorizationContext context(HttpMethod method, String path) {
        MockServerHttpRequest request = MockServerHttpRequest.method(method, path).build();
        return new AuthorizationContext(MockServerWebExchange.from(request));
    }
}
