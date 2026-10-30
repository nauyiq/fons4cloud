package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 业务运行异常
 * @author hongqy
 */
public class BusinessRuntimeException extends BsException {

    /**
     * 使用业务错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 业务错误定义
     */
    public BusinessRuntimeException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.BUSINESS));
    }

    /**
     * 使用业务错误定义和原始原因创建异常。
     * @param code 业务错误定义
     * @param cause 原始原因
     */
    public BusinessRuntimeException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.BUSINESS), cause);
    }

    /**
     * 创建业务分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public BusinessRuntimeException(String code, String message) {
        super(code, message, ExceptionType.BUSINESS);
    }

    /**
     * 根据结果创建业务异常；已有错误定义必须属于业务分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public BusinessRuntimeException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.BUSINESS));
    }

    /**
     * 根据错误结果和原始原因创建业务异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public BusinessRuntimeException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.BUSINESS), cause);
    }

}
