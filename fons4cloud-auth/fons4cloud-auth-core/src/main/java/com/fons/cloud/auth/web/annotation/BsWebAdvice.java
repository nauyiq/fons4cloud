package com.fons.cloud.auth.web.annotation;

import java.lang.annotation.*;

/**
 * 基础web加强注解
 * <p>声明接口的访问控制要求：requiredToken 标记是否需要 access_token 访问（默认需要），
 * requiredIdentifier 标记是否需要全局幂等校验（默认不需要）；
 * 由框架认证链路在启动时扫描注册到认证资源仓库。响应脱敏已由框架统一处理，不在此声明。</p>
 * @author hongqy
 * @date 2025/12/15
 */
@Documented
@Target({ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BsWebAdvice {

    /**
     * 是否需要access_token访问 默认需要
     * @return
     */
    boolean requiredToken() default true;

    /**
     * 是否需要校验全局幂等 默认不需要
     * @return
     */
    boolean requiredIdentifier() default false;
}
