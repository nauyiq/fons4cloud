package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

/**
 * admin 登录请求；clientId/clientSecret 只能由 admin 服务端配置填充。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequest extends BaseRequest {

    /**
     * 用户名、手机号或邮箱。
     */
    @NotBlank(message = "访问账号不能为空")
    private String accessAccount;

    /**
     * 密码、短信验证码或邮箱验证码。
     */
    @NotBlank(message = "访问密钥不能为空")
    @ToString.Exclude
    private String accessSecret;

    /**
     * 授权类型，取值与认证服务 GrantType 保持一致。
     */
    @NotBlank(message = "授权类型不能为空")
    private String grantType;

    /**
     * 授权范围。
     */
    private Set<String> scopes;
}
