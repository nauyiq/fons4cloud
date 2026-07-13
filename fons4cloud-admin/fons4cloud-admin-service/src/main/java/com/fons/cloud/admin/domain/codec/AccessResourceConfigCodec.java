package com.fons.cloud.admin.domain.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** 授权资源、忽略 Token URI 与幂等 URI 的分类配置契约。 */
@Component
public class AccessResourceConfigCodec extends AbstractJsonGovernanceConfigCodec {
    public AccessResourceConfigCodec(ObjectMapper objectMapper) { super(objectMapper); }
    @Override public GovernanceDomain domain() { return GovernanceDomain.ACCESS; }
    @Override public Set<String> resourceTypes() { return Set.of("AUTH_RESOURCE"); }
    @Override protected void validateNode(JsonNode root, List<ValidationIssue> issues) {
        if (!root.isObject()) {
            issues.add(new ValidationIssue("/", "ACCESS_OBJECT_REQUIRED", "访问控制配置必须为对象"));
            return;
        }
        requiredArray(root, "authorizationResources", issues);
        requiredArray(root, "ignoredAccessTokenUris", issues);
        requiredArray(root, "identifierTokenUris", issues);
    }
    private void requiredArray(JsonNode root, String field, List<ValidationIssue> issues) {
        if (!root.path(field).isArray()) issues.add(new ValidationIssue("/" + field, "ARRAY_REQUIRED", field + " 必须为数组"));
    }
}
