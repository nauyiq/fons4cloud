package com.fons.cloud.admin.infrastructure.discovery;

import com.fons.cloud.admin.api.response.ServiceInstanceResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 服务发现只读适配器，仅查询注册服务和实例，不执行上下线、权重或配置变更。
 */
@Component
public class ServiceDiscoveryReadAdapter {

    private final DiscoveryClient discoveryClient;

    /**
     * 创建服务发现只读适配器。
     *
     * @param discoveryClient Spring Cloud 服务发现客户端
     */
    public ServiceDiscoveryReadAdapter(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 查询注册中心中的服务名列表。
     *
     * @return 服务名列表
     */
    public List<String> listServices() {
        return discoveryClient.getServices();
    }

    /**
     * 查询指定服务的注册实例。
     *
     * @param serviceName 注册中心服务名
     * @return 服务实例只读视图
     */
    public List<ServiceInstanceResponse> listInstances(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return List.of();
        }
        return discoveryClient.getInstances(serviceName).stream()
                .map(this::toResponse)
                .toList();
    }

    private ServiceInstanceResponse toResponse(ServiceInstance instance) {
        return ServiceInstanceResponse.builder()
                .serviceName(instance.getServiceId())
                .instanceId(instance.getInstanceId())
                .host(instance.getHost())
                .port(instance.getPort())
                .healthy(Boolean.TRUE)
                .metadata(instance.getMetadata())
                .build();
    }
}
