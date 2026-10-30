package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 认证异常
 * @author hongqy
 */
public class AuthenticationException extends BsException {

    /**
     * 使用认证错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 认证错误定义
     */
    public AuthenticationException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.AUTHENTICATION));
    }

    /**
     * 使用认证错误定义和原始原因创建异常。
     * @param code 认证错误定义
     * @param cause 原始原因
     */
    public AuthenticationException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.AUTHENTICATION), cause);
    }

    /**
     * 创建认证分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public AuthenticationException(String code, String message) {
        super(code, message, ExceptionType.AUTHENTICATION);
    }

    /**
     * 根据结果创建认证异常；已有错误定义必须属于认证分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public AuthenticationException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.AUTHENTICATION));
    }

    /**
     * 根据错误结果和原始原因创建认证异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public AuthenticationException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.AUTHENTICATION), cause);
    }
}
