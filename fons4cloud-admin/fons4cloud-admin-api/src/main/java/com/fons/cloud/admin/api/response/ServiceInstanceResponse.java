package com.fons.cloud.admin.api.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 注册中心服务实例只读视图。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstanceResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 注册中心服务名。
     */
    private String serviceName;

    /**
     * 服务实例ID，由注册中心或服务发现实现提供。
     */
    private String instanceId;

    /**
     * 实例主机地址。
     */
    private String host;

    /**
     * 实例端口。
     */
    private Integer port;

    /**
     * 实例健康状态；只读展示，不触发上下线操作。
     */
    private Boolean healthy;

    /**
     * 注册中心元数据；仅用于展示和诊断。
     */
    private Map<String, String> metadata;
}
