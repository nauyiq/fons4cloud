package com.fons.cloud.admin.api.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Actuator 只读探测结果。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActuatorProbeResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 被探测服务名。
     */
    private String serviceName;

    /**
     * 被探测 actuator 端点路径。
     */
    private String endpointPath;

    /**
     * 探测是否可用；该结果只读，不改变服务状态。
     */
    private Boolean available;

    /**
     * 端点返回的状态文本或标准化状态。
     */
    private String status;

    /**
     * 不可用原因摘要；需避免暴露内部堆栈或敏感配置。
     */
    private String unavailableReason;
}
