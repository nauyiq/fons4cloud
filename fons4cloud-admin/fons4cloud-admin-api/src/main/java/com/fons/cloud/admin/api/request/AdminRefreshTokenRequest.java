package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * admin 刷新 Token 请求；clientId/clientSecret 只能由 admin 服务端配置填充。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefreshTokenRequest extends BaseRequest {

    /**
     * 刷新令牌；不在日志中输出。
     */
    @NotBlank(message = "刷新令牌不能为空")
    @ToString.Exclude
    private String refreshToken;
}
