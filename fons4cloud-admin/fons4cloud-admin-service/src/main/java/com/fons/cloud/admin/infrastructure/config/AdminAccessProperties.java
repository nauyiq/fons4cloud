package com.fons.cloud.admin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * admin 管理员和 ROOT 初始化配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = AdminAccessProperties.PREFIX)
public class AdminAccessProperties {

    public static final String PREFIX = "admin.access";

    /**
     * 是否启用首个 ROOT 管理员自动初始化。
     */
    private boolean rootInitializerEnabled;

    /**
     * 首个 ROOT 管理员认证账号 ID，优先级高于用户名。
     */
    private Long rootAccountId;

    /**
     * 首个 ROOT 管理员用户名。
     */
    private String rootUsername;
}
