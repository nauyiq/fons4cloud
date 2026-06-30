package com.fons.cloud.infrastructure.sms.strategt;

/**
 * @author hongqy
 */
public class MockSmsStrategy implements SmsStrategy {
    @Override
    public boolean sendCode(String type, String phoneNumber, String code) {
        return true;
    }
}
