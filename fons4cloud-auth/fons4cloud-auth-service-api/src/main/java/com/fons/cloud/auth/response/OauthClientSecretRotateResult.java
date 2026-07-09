package com.fons.cloud.auth.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth Client 密钥创建或轮换结果，plainClientSecret 只能一次性返回给调用方。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientSecretRotateResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端ID。
     */
    private String clientId;

    /**
     * 一次性返回的明文密钥；调用方必须自行安全保存，后续无法从认证服务反查。
     */
    @ToString.Exclude
    private String plainClientSecret;

    /**
     * 脱敏密钥摘要，用于页面回显和审计提示。
     */
    private String maskedClientSecret;

    /**
     * 密钥轮换后的乐观锁版本号。
     */
    private Integer version;
}
