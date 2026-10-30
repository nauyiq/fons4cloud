package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;

/**
 * 外部服务或第三方组件调用失败时使用的异常。
 * @author hongqy
 */
public class ExternalException extends BsException {

    /**
     * 使用外部错误定义创建异常，并拒绝其他分类的错误码。
     * @param exceptionCode 外部错误定义
     */
    public ExternalException(ExceptionCode exceptionCode) {
        super(ExceptionCode.of(exceptionCode, ExceptionType.EXTERNAL));
    }

    /**
     * 使用外部错误定义和原始原因创建异常。
     * @param code 外部错误定义
     * @param cause 原始原因
     */
    public ExternalException(ExceptionCode code, Throwable cause) {
        super(ExceptionCode.of(code, ExceptionType.EXTERNAL), cause);
    }

    /**
     * 创建外部依赖分类的临时错误定义。
     * @param code 错误码
     * @param message 错误说明
     */
    public ExternalException(String code, String message) {
        super(code, message, ExceptionType.EXTERNAL);
    }

    /**
     * 根据结果创建外部异常；已有错误定义必须属于外部分类。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     */
    public ExternalException(Result result) {
        super(ExceptionCode.of(result, ExceptionType.EXTERNAL));
    }

    /**
     * 根据错误结果和原始原因创建外部异常。
     * @param result 已实现 ExceptionCode 且分类一致的错误结果
     * @param cause 原始原因
     */
    public ExternalException(Result result, Throwable cause) {
        super(ExceptionCode.of(result, ExceptionType.EXTERNAL), cause);
    }

}
