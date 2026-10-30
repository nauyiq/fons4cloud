package com.fons.cloud.web.config;

import com.fons.cloud.web.advice.SensitiveResponseBodyAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 响应脱敏自动配置：注册响应体脱敏 Advice 与其配置属性。
 *
 * @author hongqy
 */
@Configuration
@EnableConfigurationProperties(SensitiveProperties.class)
public class SensitiveWebAutoConfiguration {

    /**
     * 响应脱敏 Advice 注册；@ConditionalOnMissingBean 保留业务方自定义覆盖能力。
     *
     * @param properties 脱敏配置
     * @return           响应体脱敏 Advice
     */
    @Bean
    @ConditionalOnMissingBean
    public SensitiveResponseBodyAdvice sensitiveResponseBodyAdvice(SensitiveProperties properties) {
        return new SensitiveResponseBodyAdvice(properties);
    }
}
