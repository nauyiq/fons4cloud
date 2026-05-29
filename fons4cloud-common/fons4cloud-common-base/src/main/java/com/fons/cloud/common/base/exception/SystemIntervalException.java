package com.fons.cloud.common.base.exception;

import com.fons.cloud.common.result.ResultCode;

/**
 * 系统内部异常
 * @author hongqy
 */
public class SystemIntervalException extends BizException {

    public SystemIntervalException(String message) {
        super(ResultCode.SYSTEM_INTERVAL_ERROR.code, message);
    }

    public SystemIntervalException(Throwable cause) {
        super(ResultCode.SYSTEM_INTERVAL_ERROR.code, cause);
    }

    public SystemIntervalException(String message, Throwable cause) {
        super(ResultCode.SYSTEM_INTERVAL_ERROR.code, message, cause);
    }

    public static SystemIntervalException of(String message) {
        return new SystemIntervalException(message);
    }

    public static SystemIntervalException of(String message, Throwable cause) {
        return new SystemIntervalException(message, cause);
    }

}
