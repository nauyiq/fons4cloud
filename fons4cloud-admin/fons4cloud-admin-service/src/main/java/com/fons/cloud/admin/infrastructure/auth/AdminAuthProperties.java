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

    public static final String PREFIX = "sys.admin.auth";

    /**
     * 控制面统一认证客户端 ID，当前框架标准固定为 sys-admin。
     */
    private String clientId = "sys-admin";

    /**
     * 控制面统一认证客户端密钥，只能由服务端安全配置注入。
     */
    private String clientSecret;

    /** Refresh Token Cookie 名称，不使用 __Host 前缀以兼容本地非 HTTPS 开发。 */
    private String refreshCookieName = "fons4cloud-admin-refresh";

    /** Cookie 只发送给认证端点，普通治理 API 仍只接受 Bearer Token。 */
    private String refreshCookiePath = "/admin/auth";

    /** 生产环境必须开启；本地开发可通过配置显式关闭。 */
    private boolean refreshCookieSecure = true;

    /** Cookie SameSite 策略。 */
    private String refreshCookieSameSite = "Strict";

    /** Refresh Cookie 最长存活秒数。 */
    private long refreshCookieMaxAgeSeconds = 604800L;
}
