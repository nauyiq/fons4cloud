package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * OAuth Client 新增请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientCreateRequest extends BaseRequest {

    /**
     * 客户端ID，作为 oauth_client 主键和服务接入框架账户体系的客户端标识。
     */
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 客户端明文密钥；为空时由认证服务生成并一次性返回。
     */
    @ToString.Exclude
    private String clientSecret;

    /**
     * 客户端可访问的资源ID集合，多个值沿用认证服务逗号分隔格式。
     */
    private String resourceIds;

    /**
     * 授权范围；为空时认证服务按默认 all 处理。
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
     * access_token 有效期，单位秒；为空时沿用认证服务默认值。
     */
    private Integer accessTokenValidity;

    /**
     * refresh_token 有效期，单位秒；为空时沿用认证服务默认值。
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
     * 客户端启停状态；为空时创建为启用。
     */
    private Boolean status;
}
