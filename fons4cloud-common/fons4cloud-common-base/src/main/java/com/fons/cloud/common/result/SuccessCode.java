package com.fons.cloud.common.result;

/**
 * 通用成功结果码，与只能用于失败的异常错误码分开定义。
 * @author hongqy
 */
public enum SuccessCode implements Result {

    SUCCESS("000000", "成功"),
    DUPLICATE_SUCCESS("000001", "幂等成功");

    private final String code;
    private final String message;

    SuccessCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
