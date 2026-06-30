package com.fons.cloud.infrastructure.sms.strategt;

/**
 * @author hongqy
 */
public interface SmsStrategy {

    /**
     * 发送验证码
     * @param type        消息类型
     * @param phoneNumber 手机号码
     * @param code        短信验证码
     * @return            是否发送成功
     */
    boolean sendCode(String type, String phoneNumber, String code);
}
