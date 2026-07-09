package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * OAuth Client 密钥轮换请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientRotateSecretRequest extends BaseRequest {

    /**
     * 客户端ID，作为需要轮换密钥的 oauth_client 主键。
     */
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 新明文密钥；为空时由认证服务生成并一次性返回。
     */
    @ToString.Exclude
    private String newClientSecret;

    /**
     * 乐观锁版本号。
     */
    private Integer version;
}
