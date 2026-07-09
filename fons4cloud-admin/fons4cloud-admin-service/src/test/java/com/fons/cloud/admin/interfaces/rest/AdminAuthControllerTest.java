package com.fons.cloud.admin.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.request.AdminLoginRequest;
import com.fons.cloud.admin.api.request.AdminRefreshTokenRequest;
import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.application.AdminAuthApplicationService;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * admin 认证 REST API 测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @Mock
    private AdminAuthApplicationService adminAuthApplicationService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminAuthController(adminAuthApplicationService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginShouldDelegateToApplicationService() throws Exception {
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
                .andExpect(jsonPath("$.data.username").value("root"));

        verify(adminAuthApplicationService).login(any(AdminLoginRequest.class));
    }

    @Test
    void refreshTokenShouldDelegateToApplicationService() throws Exception {
        when(adminAuthApplicationService.refreshToken(any(AdminRefreshTokenRequest.class))).thenReturn(R.ok(tokenResponse()));

        mockMvc.perform(post("/admin/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(AdminRefreshTokenRequest.builder()
                                .refreshToken("refresh-token")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminAuthApplicationService).refreshToken(any(AdminRefreshTokenRequest.class));
    }

    @Test
    void logoutShouldResolveBearerTokenAndDelegateToApplicationService() throws Exception {
        when(adminAuthApplicationService.logout("access-token")).thenReturn(R.ok(Boolean.TRUE));

        mockMvc.perform(delete("/admin/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

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
