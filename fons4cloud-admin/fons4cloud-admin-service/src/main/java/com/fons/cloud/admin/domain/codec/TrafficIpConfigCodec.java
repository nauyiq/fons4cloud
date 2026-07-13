package com.fons.cloud.admin.domain.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** 流量 IP 黑白名单配置契约。 */
@Component
public class TrafficIpConfigCodec extends AbstractJsonGovernanceConfigCodec {
    public TrafficIpConfigCodec(ObjectMapper objectMapper) { super(objectMapper); }
    @Override public GovernanceDomain domain() { return GovernanceDomain.TRAFFIC; }
    @Override public Set<String> resourceTypes() { return Set.of("IP_LIST"); }
    @Override protected void validateNode(JsonNode root, List<ValidationIssue> issues) {
        if (!root.isObject()) {
            issues.add(new ValidationIssue("/", "TRAFFIC_OBJECT_REQUIRED", "流量治理配置必须为对象"));
            return;
        }
        if (!root.path("whiteIps").isArray()) issues.add(new ValidationIssue("/whiteIps", "ARRAY_REQUIRED", "白名单必须为数组"));
        if (!root.path("manualBlockedIps").isArray()) issues.add(new ValidationIssue("/manualBlockedIps", "ARRAY_REQUIRED", "人工黑名单必须为数组"));
    }
}
