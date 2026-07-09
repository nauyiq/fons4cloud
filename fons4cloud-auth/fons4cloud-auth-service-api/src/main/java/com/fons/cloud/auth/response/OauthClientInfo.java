package com.fons.cloud.auth.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * OAuth Client 脱敏视图，不返回 clientSecret。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端ID。
     */
    private String clientId;

    /**
     * 客户端可访问的资源ID集合，多个值沿用认证服务逗号分隔格式。
     */
    private String resourceIds;

    /**
     * 授权范围。
     */
    private String scope;

    /**
     * 授权模式集合，沿用认证服务逗号分隔格式。
     */
    private String authorizedGrantTypes;

    /**
     * 授权码模式下的回调地址。
     */
    private String webServerRedirectUri;

    /**
     * Spring Security 权限值集合，沿用认证服务逗号分隔格式。
     */
    private String authorities;

    /**
     * access_token 有效期，单位秒。
     */
    private Integer accessTokenValidity;

    /**
     * refresh_token 有效期，单位秒。
     */
    private Integer refreshTokenValidity;

    /**
     * 认证扩展信息；响应不做业务解析。
     */
    private String additionalInformation;

    /**
     * 自动授权配置，沿用 OAuth2 autoapprove 语义。
     */
    private String autoapprove;

    /**
     * 客户端启停状态。
     */
    private Boolean status;

    /**
     * 逻辑删除标识。
     */
    private Boolean deleted;

    /**
     * 乐观锁版本号。
     */
    private Integer version;

    /**
     * 创建时间。
     */
    private Date created;

    /**
     * 更新时间。
     */
    private Date updated;
}
