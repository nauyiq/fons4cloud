package com.fons.cloud.admin.domain.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** OAuth Client 动作命令安全契约，明确拒绝任何明文 Secret。 */
@Component
public class OAuthClientConfigCodec extends AbstractJsonGovernanceConfigCodec {
    public OAuthClientConfigCodec(ObjectMapper objectMapper) { super(objectMapper); }
    @Override public GovernanceDomain domain() { return GovernanceDomain.CLIENTS; }
    @Override public Set<String> resourceTypes() { return Set.of("OAUTH_CLIENT"); }
    @Override protected void validateNode(JsonNode root, List<ValidationIssue> issues) {
        if (!root.isObject()) {
            issues.add(new ValidationIssue("/", "CLIENT_OBJECT_REQUIRED", "OAuth Client 配置必须为对象"));
            return;
        }
        if (root.path("operation").asText().isBlank()) issues.add(new ValidationIssue("/operation", "OPERATION_REQUIRED", "治理操作不能为空"));
        if (root.path("clientId").asText().isBlank()) issues.add(new ValidationIssue("/clientId", "CLIENT_ID_REQUIRED", "clientId 不能为空"));
        if (root.hasNonNull("clientSecret") || root.hasNonNull("newClientSecret")) {
            issues.add(new ValidationIssue("/clientSecret", "SECRET_NOT_ALLOWED", "草稿不得包含明文 Secret"));
        }
    }
}
