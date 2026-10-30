package com.fons.cloud.common.exception;

import com.fons.cloud.common.result.Result;
import lombok.Getter;

import java.util.Objects;

/**
 * 框架层提供的基础异常
 * @author hongqy
 */
@Getter
public class BsException extends RuntimeException {

    /**
     * 异常错误码
     */
    private final ExceptionCode exceptionCode;

    /**
     * 使用完整错误定义创建异常。
     * @param exceptionCode 包含分类和模块的错误定义
     */
    public BsException(ExceptionCode exceptionCode) {
        this(exceptionCode, null);
    }

    /**
     * 接受已实现 {@link ExceptionCode} 的结果，保留其分类和模块。
     * @param result 已分类的错误结果
     */
    public BsException(Result result) {
        this(ExceptionCode.of(result));
    }

    /**
     * 使用显式分类创建异常，模块默认为 DEFAULT。
     * @param code 错误码
     * @param message 错误说明
     * @param exceptionType 异常分类
     */
    public BsException(String code, String message, ExceptionType exceptionType) {
        this(ExceptionCode.of(code, message, exceptionType));
    }

    /**
     * 接受已分类的结果并保留底层原因。
     * @param result 已分类的错误结果
     * @param cause 底层异常，可为空
     */
    public BsException(Result result, Throwable cause) {
        this(ExceptionCode.of(result), cause);
    }

    /**
     * 使用完整错误定义和底层原因创建异常。
     * @param exceptionCode 包含分类和模块的错误定义
     * @param cause 底层异常，可为空
     */
    public BsException(ExceptionCode exceptionCode, Throwable cause) {
        super(Objects.requireNonNull(exceptionCode, "exceptionCode must not be null").getMessage(), cause);
        ExceptionCode.of(exceptionCode);
        Objects.requireNonNull(exceptionCode.exceptionType(), "exceptionType must not be null");
        if (exceptionCode.module() == null || exceptionCode.module().isBlank()) {
            throw new IllegalArgumentException("module must not be blank");
        }
        this.exceptionCode = exceptionCode;
    }

    /**
     * 按错误定义中的分类创建对应异常子类。
     * @param result 已分类的错误结果
     * @return 对应分类的异常
     */
    public static BsException of(Result result) {
        return of(ExceptionCode.of(result));
    }

    /**
     * 检查已分类结果的分类并创建对应异常。
     * @param result 已分类的错误结果
     * @param exceptionType 期望的分类
     * @return 对应分类的异常
     */
    public static BsException of(Result result, ExceptionType exceptionType) {
        return of(ExceptionCode.of(result, exceptionType));
    }

    /**
     * 为普通失败结果显式指定分类和模块。
     * @param result 失败结果
     * @param exceptionType 异常分类
     * @param module 错误所属模块
     * @return 对应分类的异常
     */
    public static BsException of(Result result, ExceptionType exceptionType, String module) {
        return of(ExceptionCode.of(result, exceptionType, module));
    }

    /**
     * 按错误定义中的分类创建对应异常子类。
     * @param exceptionCode 错误定义
     * @return 对应分类的异常
     */
    public static BsException of(ExceptionCode exceptionCode) {
        return of(exceptionCode, (Throwable) null);
    }

    /**
     * 按错误定义创建对应异常子类并保留底层原因。
     * @param exceptionCode 错误定义
     * @param cause 底层异常，可为空
     * @return 对应分类的异常
     */
    public static BsException of(ExceptionCode exceptionCode, Throwable cause) {
        Objects.requireNonNull(exceptionCode, "exceptionCode must not be null");
        ExceptionType type = Objects.requireNonNull(exceptionCode.exceptionType(), "exceptionType must not be null");
        return switch (type) {
            case SYSTEM -> new SystemIntervalException(exceptionCode, cause);
            case VALIDATION -> new ValidationException(exceptionCode, cause);
            case AUTHENTICATION -> new AuthenticationException(exceptionCode, cause);
            case AUTHORIZATION -> new AuthorizationException(exceptionCode, cause);
            case BUSINESS -> new BusinessRuntimeException(exceptionCode, cause);
            case EXTERNAL -> new ExternalException(exceptionCode, cause);
            case UNKNOWN -> new BsException(exceptionCode, cause);
        };
    }


}
