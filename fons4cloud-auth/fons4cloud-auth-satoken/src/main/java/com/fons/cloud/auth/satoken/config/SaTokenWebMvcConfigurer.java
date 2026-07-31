package com.fons.cloud.auth.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截配置。
 * <p>
 * 按 {@link FonsSaTokenProperties} 配置注册 {@link SaInterceptor}，可选对拦截路径强制登录校验；
 * 细粒度权限/角色仍可通过 {@code @SaCheckPermission}、{@code @SaCheckRole} 注解声明。
 *
 * @author fons
 */
@RequiredArgsConstructor
public class SaTokenWebMvcConfigurer implements WebMvcConfigurer {

    private final FonsSaTokenProperties properties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    // 全局登录校验：开启时对所有拦截路径强制校验登录状态
                    if (properties.isGlobalLoginCheck()) {
                        StpUtil.checkLogin();
                    }
                }))
                .addPathPatterns(properties.getIncludePaths())
                .excludePathPatterns(properties.getExcludePaths());
    }
}
