package com.fons.cloud.admin.infrastructure.actuator;

import com.fons.cloud.admin.api.request.ActuatorProbeRequest;
import com.fons.cloud.admin.api.response.ActuatorProbeResult;
import com.fons.cloud.admin.infrastructure.discovery.ServiceDiscoveryReadAdapter;
import com.fons.cloud.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Actuator 只读探测适配器，只允许访问服务端白名单内的只读端点。
 */
@Component
public class ActuatorReadAdapter {

    private static final int DEFAULT_TIMEOUT_MS = 3000;
    private static final String DEFAULT_ENDPOINT = "/actuator/health";

    private final ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter;
    private final RestTemplate restTemplate;
    private final Set<String> allowedEndpoints;

    /**
     * 创建 Actuator 只读探测适配器。
     *
     * @param serviceDiscoveryReadAdapter 服务发现只读适配器
     * @param allowedEndpoints            允许探测的只读端点，逗号分隔
     */
    @Autowired
    public ActuatorReadAdapter(ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter,
                               @Value("${admin.actuator.probe.allowed-endpoints:/actuator/health,/actuator/info}")
                               String allowedEndpoints) {
        this(serviceDiscoveryReadAdapter, restTemplate(DEFAULT_TIMEOUT_MS), parseAllowedEndpoints(allowedEndpoints));
    }

    ActuatorReadAdapter(ServiceDiscoveryReadAdapter serviceDiscoveryReadAdapter, RestTemplate restTemplate,
                        Set<String> allowedEndpoints) {
        this.serviceDiscoveryReadAdapter = serviceDiscoveryReadAdapter;
        this.restTemplate = restTemplate;
        this.allowedEndpoints = allowedEndpoints == null || allowedEndpoints.isEmpty()
                ? Set.of(DEFAULT_ENDPOINT)
                : allowedEndpoints;
    }

    /**
     * 对指定服务实例执行只读 Actuator 探测。
     *
     * @param request 探测请求
     * @return 探测结果
     */
    public ActuatorProbeResult probe(ActuatorProbeRequest request) {
        String endpointPath = normalizeEndpoint(request.getEndpointPath());
        if (!allowedEndpoints.contains(endpointPath)) {
            return unavailable(request.getServiceName(), endpointPath, "Actuator 端点不在允许探测范围内");
        }
        List<com.fons.cloud.admin.api.response.ServiceInstanceResponse> instances =
                serviceDiscoveryReadAdapter.listInstances(request.getServiceName());
        if (instances.isEmpty()) {
            return unavailable(request.getServiceName(), endpointPath, "未发现可探测服务实例");
        }
        URI targetUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instances.get(0).getHost())
                .port(instances.get(0).getPort())
                .path(endpointPath)
                .build()
                .toUri();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(targetUri, String.class);
            boolean available = response.getStatusCode().is2xxSuccessful();
            return ActuatorProbeResult.builder()
                    .serviceName(request.getServiceName())
                    .endpointPath(endpointPath)
                    .available(available)
                    .status(statusText(response.getBody(), response.getStatusCode().toString()))
                    .unavailableReason(available ? null : "Actuator 返回非成功状态")
                    .build();
        } catch (RestClientException ex) {
            return unavailable(request.getServiceName(), endpointPath, safeMessage(ex));
        }
    }

    private ActuatorProbeResult unavailable(String serviceName, String endpointPath, String reason) {
        return ActuatorProbeResult.builder()
                .serviceName(serviceName)
                .endpointPath(endpointPath)
                .available(false)
                .status("UNAVAILABLE")
                .unavailableReason(reason)
                .build();
    }

    private String statusText(String body, String fallback) {
        if (StringUtils.isBlank(body)) {
            return fallback;
        }
        try {
            Map<String, Object> json = JsonUtil.jsonToMap(body);
            Object status = json.get("status");
            if (status != null) {
                return String.valueOf(status);
            }
        } catch (RuntimeException ignored) {
            // 非 JSON 响应只截断展示，避免把大响应体写入探测结果。
        }
        return body.length() > 256 ? body.substring(0, 256) : body;
    }

    private String normalizeEndpoint(String endpointPath) {
        String endpoint = StringUtils.defaultIfBlank(endpointPath, DEFAULT_ENDPOINT).trim();
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }

    private String safeMessage(Exception ex) {
        return StringUtils.defaultIfBlank(ex.getMessage(), "Actuator 探测失败");
    }

    private static RestTemplate restTemplate(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    private static Set<String> parseAllowedEndpoints(String allowedEndpoints) {
        if (StringUtils.isBlank(allowedEndpoints)) {
            return Set.of(DEFAULT_ENDPOINT);
        }
        return Arrays.stream(allowedEndpoints.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(endpoint -> endpoint.startsWith("/") ? endpoint : "/" + endpoint)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
