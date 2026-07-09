package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * OAuth Client 启停请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientStatusRequest extends BaseRequest {

    /**
     * 客户端ID，作为需要启停的 oauth_client 主键。
     */
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 客户端启停状态：true=启用，false=禁用。
     */
    @NotNull(message = "客户端状态不能为空")
    private Boolean status;

    /**
     * 乐观锁版本号。
     */
    private Integer version;
}
