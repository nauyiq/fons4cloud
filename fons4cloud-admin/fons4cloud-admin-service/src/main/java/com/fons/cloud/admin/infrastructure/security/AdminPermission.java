package com.fons.cloud.admin.infrastructure.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * admin-service 内部 RBAC 权限声明。
 *
 * <p>该注解只由 admin MVC 拦截器读取，不会注册到 gateway 全局授权资源。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminPermission {

    /**
     * 当前 admin API 需要的内部权限码，格式为 `{domain}:{action}`。
     *
     * @return admin 内部权限码列表
     */
    /**
     * 显式空数组表示仅要求账号已绑定且启用；未声明本注解的接口仍默认拒绝。
     *
     * @return admin 内部权限码列表
     */
    String[] authorities();
}
