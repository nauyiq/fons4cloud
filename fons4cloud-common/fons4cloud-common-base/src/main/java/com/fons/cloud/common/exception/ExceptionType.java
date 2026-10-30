package com.fons.cloud.common.exception;

/**
 * 异常类型
 * @author hongqy
 */
public enum ExceptionType {

    /***
     * 系统异常
     */
    SYSTEM,

    /**
     * 校验异常
     */
    VALIDATION,

    /**
     * 业务异常
     */
    BUSINESS,

    /**
     * 认证异常
     */
    AUTHENTICATION,

    /**
     * 已确认身份但无权执行操作
     */
    AUTHORIZATION,

    /**
     * 外部调用等第三方组件异常
     */
    EXTERNAL,

    /**
     * 未知异常
     */
    UNKNOWN,




}
