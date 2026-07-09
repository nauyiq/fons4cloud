package com.fons.cloud.admin.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * admin Web 安全配置，只对控制面 API 注册 admin 自有 RBAC 拦截器。
 */
@Configuration
@RequiredArgsConstructor
public class AdminWebSecurityConfiguration implements WebMvcConfigurer {

    private final AdminSecurityInterceptor adminSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSecurityInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login", "/admin/auth/refresh-token", "/admin/auth/logout");
    }
}
