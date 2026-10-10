package com.fons.cloud.auth.satoken.config;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;
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
        SaInterceptor saInterceptor = new SaInterceptor(handler -> {
                    // 全局登录校验：开启时对所有拦截路径强制校验登录状态
                    if (properties.isGlobalLoginCheck()) {
                        StpUtil.checkLogin();
                    }
                });

        registry.addInterceptor(new ErrorDispatchSafeInterceptor(saInterceptor))
                .addPathPatterns(properties.getIncludePaths())
                .excludePathPatterns(properties.getExcludePaths());
    }

    /**
     * 包装 {@link SaInterceptor}，在 ERROR / ASYNC 调度类型下跳过认证逻辑，
     * 避免 Tomcat forward 到 /error 时因 SaToken 上下文未初始化而产生二次异常。
     */
    @RequiredArgsConstructor
    private static class ErrorDispatchSafeInterceptor implements HandlerInterceptor {

        private final SaInterceptor delegate;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            DispatcherType dispatcherType = request.getDispatcherType();
            if (dispatcherType == DispatcherType.ERROR || dispatcherType == DispatcherType.ASYNC || CorsUtils.isPreFlightRequest(request)) {
                return true;
            }
            try {
                return delegate.preHandle(request, response, handler);
            } catch (SaTokenContextException e) {
                // SaToken 上下文未初始化时放行，避免覆盖原始异常或中断正常错误处理流程
                return true;
            }
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                    Object handler, Exception ex) throws Exception {
            DispatcherType dispatcherType = request.getDispatcherType();
            if (dispatcherType != DispatcherType.ERROR && dispatcherType != DispatcherType.ASYNC) {
                delegate.afterCompletion(request, response, handler, ex);
            }
        }
    }
}
