package com.fons.cloud.admin.domain.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 网关路由数组的服务端字段契约。 */
@Component
public class GatewayRouteConfigCodec extends AbstractJsonGovernanceConfigCodec {

    public GatewayRouteConfigCodec(ObjectMapper objectMapper) { super(objectMapper); }
    @Override public GovernanceDomain domain() { return GovernanceDomain.GATEWAY; }
    @Override public Set<String> resourceTypes() { return Set.of("ROUTE"); }

    @Override
    protected void validateNode(JsonNode root, List<ValidationIssue> issues) {
        if (!root.isArray() || root.isEmpty()) {
            issues.add(new ValidationIssue("/", "ROUTE_ARRAY_REQUIRED", "路由配置必须为非空数组"));
            return;
        }
        Set<String> routeIds = new HashSet<>();
        for (int index = 0; index < root.size(); index++) {
            JsonNode route = root.get(index);
            String path = "/" + index;
            requiredText(route, "id", path, issues);
            requiredText(route, "uri", path, issues);
            if (!route.path("predicates").isArray() || route.path("predicates").isEmpty()) {
                issues.add(new ValidationIssue(path + "/predicates", "ROUTE_PREDICATES_REQUIRED", "路由 predicates 不能为空"));
            }
            String id = route.path("id").asText();
            if (!id.isBlank() && !routeIds.add(id)) {
                issues.add(new ValidationIssue(path + "/id", "ROUTE_ID_DUPLICATED", "路由 ID 不能重复"));
            }
        }
    }

    private void requiredText(JsonNode node, String field, String path, List<ValidationIssue> issues) {
        if (!node.isObject() || node.path(field).asText().isBlank()) {
            issues.add(new ValidationIssue(path + "/" + field, "ROUTE_FIELD_REQUIRED", field + " 不能为空"));
        }
    }
}
