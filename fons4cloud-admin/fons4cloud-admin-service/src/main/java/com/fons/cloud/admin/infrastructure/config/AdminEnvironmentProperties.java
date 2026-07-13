package com.fons.cloud.admin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 单部署单环境的非敏感展示配置。 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sys.admin.environment")
public class AdminEnvironmentProperties {

    /** 页面固定展示的环境名称，不从 URL、密钥或 profile 猜测。 */
    private String name = "未命名环境";
}
