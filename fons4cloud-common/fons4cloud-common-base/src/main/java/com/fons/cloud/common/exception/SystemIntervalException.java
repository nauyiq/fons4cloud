package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 系统内部异常
 * @author hongqy
 */
public class SystemIntervalException extends BsException {

    /**
     * 使用系统错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 系统错误定义
     */
    public SystemIntervalException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.SYSTEM));
    }

    /**
     * 使用系统错误定义和原始原因创建异常。
     * @param code 系统错误定义
     * @param cause 原始原因
     */
    public SystemIntervalException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.SYSTEM), cause);
    }

    /**
     * 创建系统分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public SystemIntervalException(String code, String message) {
        super(code, message, ExceptionType.SYSTEM);
    }

    /**
     * 根据结果创建系统异常；已有错误定义必须属于系统分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public SystemIntervalException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.SYSTEM));
    }

    /**
     * 根据错误结果和原始原因创建系统异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public SystemIntervalException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.SYSTEM), cause);
    }

}
