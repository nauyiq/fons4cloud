package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;
import com.fons.cloud.common.result.SuccessCode;
import com.fons.cloud.common.response.Response;

import java.util.Objects;

/**
 * 统一的异常错误码
 * @author hongqy
 */
public interface ExceptionCode extends Result {

    /**
     * 异常类型
     * @return {@link ExceptionType}
     */
    ExceptionType exceptionType();

    /**
     * 业务模块， 业务标识
     * @return
     */
    default String module() {
        return "DEFAULT";
    }


    /**
     * 保留已有错误定义；普通结果没有足够的分类信息，不能直接作为异常码。
     * @param result 已实现 {@link ExceptionCode} 的结果
     * @return 原错误定义
     */
    static ExceptionCode of(Result result) {
        Objects.requireNonNull(result, "result must not be null");
        if (result instanceof ExceptionCode exceptionCode) {
            requireFailureCode(exceptionCode.getCode());
            return exceptionCode;
        }
        throw new IllegalArgumentException("result must be an ExceptionCode; specify type and module for other failure results");
    }

    /**
     * 使用期望分类检查已有错误定义，保留其原有模块。
     * @param result 已实现 {@link ExceptionCode} 的结果
     * @param exceptionType 期望的异常分类
     * @return 原错误定义
     */
    static ExceptionCode of(Result result, ExceptionType exceptionType) {
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        ExceptionCode exceptionCode = of(result);
        if (exceptionCode.exceptionType() != exceptionType) {
            throw new IllegalArgumentException("exception type does not match the error code");
        }
        return exceptionCode;
    }

    /**
     * 将普通失败结果转成异常码；调用方须提供分类和模块。
     * 已有异常码的分类和模块不可被覆盖。
     * @param result 失败结果
     * @param exceptionType 异常分类
     * @param module 错误所属模块
     * @return 异常码
     */
    static ExceptionCode of(Result result, ExceptionType exceptionType, String module) {
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        requireModule(module);
        if (result instanceof ExceptionCode exceptionCode) {
            ExceptionCode checked = of(exceptionCode, exceptionType);
            if (!module.equals(checked.module())) {
                throw new IllegalArgumentException("module does not match the error code");
            }
            return checked;
        }
        if (result instanceof SuccessCode || result instanceof Response response && response.isSuccess()) {
            throw new IllegalArgumentException("successful result cannot be an exception code");
        }
        return of(result.getCode(), result.getMessage(), exceptionType, module);
    }

    /**
     * 创建明确分类、使用默认模块的异常码。
     * @param code 错误码
     * @param message 错误说明
     * @param exceptionType 异常分类
     * @return 异常码
     */
    static ExceptionCode of(String code, String message, ExceptionType exceptionType) {
        return of(code, message, exceptionType, "DEFAULT");
    }

    /**
     * 创建包含明确分类和模块的异常码。
     * @param code 错误码
     * @param message 错误说明
     * @param exceptionType 异常分类
     * @param module 错误所属模块
     * @return 异常码
     */
    static ExceptionCode of(String code, String message, ExceptionType exceptionType, String module) {
        requireFailureCode(code);
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        requireModule(module);
        return new ExceptionCode() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public ExceptionType exceptionType() {
                return exceptionType;
            }

            @Override
            public String module() {
                return module;
            }
        };
    }

    private static void requireFailureCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (SuccessCode.SUCCESS.getCode().equals(code) || SuccessCode.DUPLICATE_SUCCESS.getCode().equals(code)) {
            throw new IllegalArgumentException("successful result code cannot be an exception code");
        }
    }

    private static void requireModule(String module) {
        if (module == null || module.isBlank()) {
            throw new IllegalArgumentException("module must not be blank");
        }
    }
}
