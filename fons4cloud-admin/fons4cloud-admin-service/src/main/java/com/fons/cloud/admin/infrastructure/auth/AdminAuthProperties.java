package com.fons.cloud.admin.infrastructure.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * admin 调用认证服务时使用的服务端客户端配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = AdminAuthProperties.PREFIX)
public class AdminAuthProperties {

    public static final String PREFIX = "admin.auth";

    /**
     * 控制面统一认证客户端 ID，当前框架标准固定为 sys-admin。
     */
    private String clientId = "sys-admin";

    /**
     * 控制面统一认证客户端密钥，只能由服务端安全配置注入。
     */
    private String clientSecret;
}
