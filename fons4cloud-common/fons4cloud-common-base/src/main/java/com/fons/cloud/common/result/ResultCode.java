package com.fons.cloud.common.result;

import com.fons.cloud.common.exception.ExceptionCode;
import com.fons.cloud.common.exception.ExceptionType;

/**
 * 接口级别错误码
 * @author hongqy
 */
public enum ResultCode implements ExceptionCode {

    //  ==================== 参数异常 ====================
    PARAMS_ERROR(ExceptionType.VALIDATION, "100001", "参数错误"),
    PARAM_UNDEFINED(ExceptionType.VALIDATION,"100002", "参数未定义或空"),
    DUPLICATED_REQUEST(ExceptionType.VALIDATION,"100003", "重复的请求"),
    ILLEGAL_REQUEST_LIMITED(ExceptionType.VALIDATION,"100004", "不合法的请求"),


    //  ==================== 数据异常 ====================
    INVALID_DATA(ExceptionType.BUSINESS,"200001", "无效的数据"),

    //  ==================== 认证异常 ====================
    INVALID_ACCESS_TOKEN(ExceptionType.AUTHENTICATION,"400001", "令牌不存在或已过期"),
    NOT_PERMISSION(ExceptionType.AUTHORIZATION,"400002", "没有权限访问"),
    SENTINEL_LIMITED_AUTHORITY(ExceptionType.AUTHORIZATION,"400003", "没有权限, 禁止访问"),


    //  ==================== 文件/oss异常 ====================
    FILE_IS_EMPTY(ExceptionType.VALIDATION,"500001", "不能上传空文件"),
    NOT_SUPPORT_FILE_TYPE(ExceptionType.VALIDATION,"500002", "不支持的文件类型"),
    UPLOAD_FAILED(ExceptionType.BUSINESS,"500003", "文件上传失败"),
    UPLOAD_EXCEPTION(ExceptionType.BUSINESS,"500004", "文件上传异常"),
    DOWNLOAD_FAILED(ExceptionType.BUSINESS,"500005", "文件下载失败"),

    // ==================== 外部错误 ====================


    // ==================== 限流熔断错误 ====================
    TOO_MANY_REQUEST(ExceptionType.SYSTEM,"800001", "请求太频繁, 稍后再试"),
    INTERFACE_BUSY_LIMIT(ExceptionType.SYSTEM,"800002", "接口繁忙,稍后再试"),


    //  ==================== 系统异常 ====================
    FAILED(ExceptionType.SYSTEM,"999991", "失败"),
    INSERT_FAILED(ExceptionType.SYSTEM,"999002", "新增数据失败"),
    UPDATE_FAILED(ExceptionType.SYSTEM,"999003", "更新数据失败"),

    UNKNOWN_BUSINESS_EXCEPTION(ExceptionType.BUSINESS,"999004", "未知业务异常"),
    SYSTEM_BUSY(ExceptionType.SYSTEM,"999990", "系统繁忙"),
    SYSTEM_INTERVAL_ERROR(ExceptionType.SYSTEM,"999999", "系统内部错误"),


    ;

    ResultCode(ExceptionType exceptionType, String code, String message) {
        this.exceptionType = exceptionType;
        this.module = "CORE";
        this.code = code;
        this.message = message;
    }


    private final ExceptionType exceptionType;
    private final String module;
    private final String code;
    private final String message;


    @Override
    public ExceptionType exceptionType() {
        return exceptionType;
    }

    @Override
    public String module() {
        return module;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }
}
