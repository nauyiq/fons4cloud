package com.fons.cloud.infrastructure.constants;

import com.fons.cloud.common.result.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hongqy
 */
@Getter
@AllArgsConstructor
public enum InfrastructureResultCode implements Result {


    //  ==================== 参数异常 ====================
    INCORRECT_PHONE("CM100001", "无效的手机号码"),

    //  ==================== 业务异常 ====================
    FAILED_SEND_SMS("CM300001", "发送短信失败"),
    NOT_FOUND_SMS_TEMPLATE("CM300002", "短信模板不存在")
    ;

    public final String code;

    public final String message;

}
