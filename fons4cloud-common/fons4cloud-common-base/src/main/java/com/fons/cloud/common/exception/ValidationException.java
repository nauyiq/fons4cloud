package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 验证异常
 * @author hongqy
 */
public class ValidationException extends BsException {

    /**
     * 使用校验错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 校验错误定义
     */
    public ValidationException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.VALIDATION));
    }

    /**
     * 使用校验错误定义和原始原因创建异常。
     * @param code 校验错误定义
     * @param cause 原始原因
     */
    public ValidationException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.VALIDATION), cause);
    }

    /**
     * 创建校验分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public ValidationException(String code, String message) {
        super(code, message, ExceptionType.VALIDATION);
    }

    /**
     * 根据结果创建校验异常；已有错误定义必须属于校验分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public ValidationException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.VALIDATION));
    }

    /**
     * 根据错误结果和原始原因创建校验异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public ValidationException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.VALIDATION), cause);
    }

}
