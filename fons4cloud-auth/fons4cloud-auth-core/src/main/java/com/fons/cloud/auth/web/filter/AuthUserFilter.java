package com.fons.cloud.auth.web.filter;

import com.fons.cloud.auth.utils.AuthUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * 认证用户上下文过滤器
 * <p>请求结束时清理 AuthUtils 中的 ThreadLocal 认证用户，
 * 防止 Tomcat 线程池复用导致用户信息跨请求泄漏。</p>
 * @author hongqy
 * @date 2024/10/9
 */
public class AuthUserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // 移除上下文认证用户
            AuthUtils.removeUser();
        }
    }
}
