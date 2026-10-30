package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 已认证主体没有访问权限时使用的授权异常。
 * @author hongqy
 */
public class AuthorizationException extends BsException {

    /**
     * 使用授权错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 授权错误定义
     */
    public AuthorizationException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.AUTHORIZATION));
    }

    /**
     * 使用授权错误定义和原始原因创建异常。
     * @param code 授权错误定义
     * @param cause 原始原因
     */
    public AuthorizationException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.AUTHORIZATION), cause);
    }

    /**
     * 创建授权分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public AuthorizationException(String code, String message) {
        super(code, message, ExceptionType.AUTHORIZATION);
    }

    /**
     * 根据结果创建授权异常；已有错误定义必须属于授权分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public AuthorizationException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.AUTHORIZATION));
    }

    /**
     * 根据错误结果和原始原因创建授权异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public AuthorizationException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.AUTHORIZATION), cause);
    }
}
