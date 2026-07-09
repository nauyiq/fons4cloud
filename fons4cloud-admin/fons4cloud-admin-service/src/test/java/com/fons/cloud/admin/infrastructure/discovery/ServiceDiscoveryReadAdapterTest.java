package com.fons.cloud.admin.infrastructure.discovery;

import com.fons.cloud.admin.api.response.ServiceInstanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 服务发现只读适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class ServiceDiscoveryReadAdapterTest {

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;

    private ServiceDiscoveryReadAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServiceDiscoveryReadAdapter(discoveryClient);
    }

    @Test
    void listServicesShouldReturnDiscoveryServiceNames() {
        when(discoveryClient.getServices()).thenReturn(List.of("fons4cloud-auth", "fons4cloud-gateway"));

        assertThat(adapter.listServices()).containsExactly("fons4cloud-auth", "fons4cloud-gateway");
    }

    @Test
    void listInstancesShouldMapServiceInstanceFields() {
        when(discoveryClient.getInstances("fons4cloud-auth")).thenReturn(List.of(serviceInstance));
        when(serviceInstance.getServiceId()).thenReturn("fons4cloud-auth");
        when(serviceInstance.getInstanceId()).thenReturn("127.0.0.1:18080");
        when(serviceInstance.getHost()).thenReturn("127.0.0.1");
        when(serviceInstance.getPort()).thenReturn(18080);
        when(serviceInstance.getMetadata()).thenReturn(Map.of("version", "local"));

        List<ServiceInstanceResponse> responses = adapter.listInstances("fons4cloud-auth");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getServiceName()).isEqualTo("fons4cloud-auth");
        assertThat(responses.get(0).getHost()).isEqualTo("127.0.0.1");
        assertThat(responses.get(0).getPort()).isEqualTo(18080);
        assertThat(responses.get(0).getHealthy()).isTrue();
        assertThat(responses.get(0).getMetadata()).containsEntry("version", "local");
    }
}
