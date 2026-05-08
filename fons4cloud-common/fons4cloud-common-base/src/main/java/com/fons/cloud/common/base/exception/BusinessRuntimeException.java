package com.fons.cloud.common.base.exception;

import cn.hutool.core.lang.Assert;
import com.fons.cloud.common.result.Result;
import com.fons.cloud.common.result.ResultCode;
import org.apache.commons.lang3.StringUtils;

/**
 * 业务运行时异常
 * @author hongqy
 */
public class BusinessRuntimeException extends BizException {


    public BusinessRuntimeException(String code) {
        super(code);
    }

    public BusinessRuntimeException(String code, String message) {
        super(code, message);
    }

    public BusinessRuntimeException(Result result) {
        super(result);
    }

    public BusinessRuntimeException(String code, Throwable cause) {
        super(code, cause);
    }

    public BusinessRuntimeException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static BusinessRuntimeException of(Throwable e) {
        return new BusinessRuntimeException(ResultCode.UNKNOWN_BUSINESS_EXCEPTION.code, e);
    }

    public static BusinessRuntimeException of(Result result) {
        Assert.notNull(result, () -> new BusinessRuntimeException(ResultCode.UNKNOWN_BUSINESS_EXCEPTION));
        return new BusinessRuntimeException(result);
    }

    public static BusinessRuntimeException of(String code, String message) {
        code = StringUtils.isBlank(code) ? ResultCode.UNKNOWN_BUSINESS_EXCEPTION.getCode() : code;
        return new BusinessRuntimeException(code, message);
    }

    public static BusinessRuntimeException of(String code, Throwable cause) {
        code = StringUtils.isBlank(code) ? ResultCode.UNKNOWN_BUSINESS_EXCEPTION.getCode() : code;
        return new BusinessRuntimeException(code, cause);
    }

    public static BusinessRuntimeException of(String code, String message, Throwable cause) {
        code = StringUtils.isBlank(code) ? ResultCode.UNKNOWN_BUSINESS_EXCEPTION.getCode() : code;
        return new BusinessRuntimeException(code, message, cause);
    }

}
