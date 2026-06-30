package com.fons.cloud.infrastructure.sms.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hongqy
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = SmsProperties.PREFIX)
public class SmsProperties {
    public static final String PREFIX = "sys.infrastructure.sms";

    private String host;

    private String path;

    private String appcode;

    private Map<String, SmsTemplate> templates = new HashMap<>();


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsTemplate {

        /**
         * 模板名称
         */
        private String name;

        /**
         * 短信模板ID
         */
        private String templateId;

        /**
         * 短信签名ID
         */
        private String smsSignId;



    }

}
