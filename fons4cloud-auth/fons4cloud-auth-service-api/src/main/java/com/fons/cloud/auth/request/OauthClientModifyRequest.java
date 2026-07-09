package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * OAuth Client 基础信息修改请求，不承载 clientSecret。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientModifyRequest extends BaseRequest {

    /**
     * 客户端ID，作为需要修改的 oauth_client 主键。
     */
    @NotBlank(message = "客户端ID不能为空")
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
     * 认证扩展信息，必须保持认证服务可解析的 JSON 字符串。
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
     * 乐观锁版本号。
     */
    private Integer version;
}
