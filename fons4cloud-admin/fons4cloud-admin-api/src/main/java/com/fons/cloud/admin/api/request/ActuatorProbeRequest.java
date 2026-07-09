package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Actuator 只读探测请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActuatorProbeRequest extends BaseRequest {

    /**
     * 被探测服务名；必须来自允许探测的注册服务范围。
     */
    @NotBlank(message = "服务名不能为空")
    private String serviceName;

    /**
     * 探测路径，只允许服务端白名单内的只读端点。
     */
    private String endpointPath;
}
