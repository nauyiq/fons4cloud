package com.fons.cloud.admin.infrastructure.actuator;

import com.fons.cloud.admin.api.request.ActuatorProbeRequest;
import com.fons.cloud.admin.api.response.ActuatorProbeResult;
import com.fons.cloud.admin.api.response.ServiceInstanceResponse;
import com.fons.cloud.admin.infrastructure.discovery.ServiceDiscoveryReadAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Actuator 只读探测适配器测试。
 */
@ExtendWith(MockitoExtension.class)
class ActuatorReadAdapterTest {

    @Mock
    private ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter;
    @Mock
    private RestTemplate restTemplate;

    private ActuatorReadAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ActuatorReadAdapter(serviceDiscoveryReadAdapter, restTemplate,
                Set.of("/actuator/health", "/actuator/info"));
    }

    @Test
    void springContextShouldCreateAdapterWithServiceDiscoveryConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "admin.actuator.probe.allowed-endpoints=/actuator/health,/actuator/info");
            context.registerBean(ServiceDiscoveryReadAdapter.class, () -> serviceDiscoveryReadAdapter);
            context.register(ActuatorReadAdapter.class);

            context.refresh();

            assertThat(context.getBean(ActuatorReadAdapter.class)).isNotNull();
        }
    }

    @Test
    void probeShouldReturnUpForAllowedEndpoint() {
        when(serviceDiscoveryReadAdapter.listInstances("fons4cloud-auth")).thenReturn(List.of(instance()));
        when(restTemplate.getForEntity(URI.create("http://127.0.0.1:18080/actuator/health"), String.class))
                .thenReturn(ResponseEntity.ok("{\"status\":\"UP\"}"));

        ActuatorProbeResult result = adapter.probe(ActuatorProbeRequest.builder()
                .serviceName("fons4cloud-auth")
                .endpointPath("/actuator/health")
                .build());

        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getStatus()).isEqualTo("UP");
        assertThat(result.getUnavailableReason()).isNull();
    }

    @Test
    void probeShouldRejectEndpointOutsideAllowList() {
        ActuatorProbeResult result = adapter.probe(ActuatorProbeRequest.builder()
                .serviceName("fons4cloud-auth")
                .endpointPath("/actuator/env")
                .build());

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getUnavailableReason()).contains("不在允许探测范围");
    }

    @Test
    void probeShouldReturnUnavailableWhenNoInstanceExists() {
        when(serviceDiscoveryReadAdapter.listInstances("fons4cloud-auth")).thenReturn(List.of());

        ActuatorProbeResult result = adapter.probe(ActuatorProbeRequest.builder()
                .serviceName("fons4cloud-auth")
                .endpointPath("/actuator/health")
                .build());

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getUnavailableReason()).contains("未发现");
    }

    private ServiceInstanceResponse instance() {
        return ServiceInstanceResponse.builder()
                .serviceName("fons4cloud-auth")
                .instanceId("127.0.0.1:18080")
                .host("127.0.0.1")
                .port(18080)
                .healthy(true)
                .build();
    }
}
