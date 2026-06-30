package com.fons.cloud.infrastructure.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 发送验证码请求
 * @author hongqy
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendAuthSmsRequest extends BaseRequest {

    /**
     * 客户端ID
     */
    @NotNull(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 手机号码
     */
    @NotNull(message = "手机号码不能为空")
    private String phone;

    /**
     * 短信有效期, 不传默认为10分钟有效
     */
    private Integer expiredSeconds;
}
