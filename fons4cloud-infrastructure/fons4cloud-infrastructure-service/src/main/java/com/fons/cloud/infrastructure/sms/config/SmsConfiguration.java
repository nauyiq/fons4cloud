package com.fons.cloud.infrastructure.sms.config;

import com.fons.cloud.infrastructure.sms.strategt.MockSmsStrategy;
import com.fons.cloud.infrastructure.sms.strategt.SmsStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hongqy
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfiguration {

    @Bean
    public SmsStrategy smsStrategy(SmsProperties smsProperties) {
        // TODO 暂时使用MOCK策略
        return new MockSmsStrategy();
    }

}
