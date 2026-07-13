package com.fons.cloud.admin.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.application.AdminAuthApplicationService;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthProperties;
import com.fons.cloud.common.result.R;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** admin 浏览器会话 REST 契约测试。 */
@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @Mock
    private AdminAuthApplicationService adminAuthApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setRefreshCookieSecure(false);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminAuthController(adminAuthApplicationService, properties)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginShouldSetHttpOnlyRefreshCookieAndHideRefreshTokenFromBody() throws Exception {
        when(adminAuthApplicationService.login(any(AdminLoginRequest.class))).thenReturn(R.ok(tokenResponse()));

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(AdminLoginRequest.builder()
                                .accessAccount("root")
                                .accessSecret("password")
                                .grantType("PASSWORD")
                                .scopes(Set.of("all"))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(10L))
                .andExpect(jsonPath("$.data.username").value("root"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("fons4cloud-admin-refresh=refresh-token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("SameSite=Strict"),
                        org.hamcrest.Matchers.containsString("Path=/admin/auth"))));
    }

    @Test
    void refreshTokenShouldReadCookieAndRotateIt() throws Exception {
        when(adminAuthApplicationService.refreshToken(any(AdminRefreshTokenRequest.class)))
                .thenReturn(R.ok(tokenResponse()));

        mockMvc.perform(post("/admin/auth/refresh-token")
                        .cookie(new Cookie("fons4cloud-admin-refresh", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("fons4cloud-admin-refresh=refresh-token")));

        ArgumentCaptor<AdminRefreshTokenRequest> captor = ArgumentCaptor.forClass(AdminRefreshTokenRequest.class);
        verify(adminAuthApplicationService).refreshToken(captor.capture());
        assertThat(captor.getValue().getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void refreshTokenShouldRejectMissingCookieWithDedicatedCode() throws Exception {
        mockMvc.perform(post("/admin/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AD100008"));
    }

    @Test
    void logoutShouldResolveBearerTokenAndClearRefreshCookie() throws Exception {
        when(adminAuthApplicationService.logout("access-token")).thenReturn(R.ok(Boolean.TRUE));

        mockMvc.perform(delete("/admin/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("fons4cloud-admin-refresh="),
                        org.hamcrest.Matchers.containsString("Max-Age=0"))));

        verify(adminAuthApplicationService).logout("access-token");
    }

    private AdminTokenResponse tokenResponse() {
        return AdminTokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .scopes(Set.of("all"))
                .userId(10L)
                .username("root")
                .build();
    }
}
