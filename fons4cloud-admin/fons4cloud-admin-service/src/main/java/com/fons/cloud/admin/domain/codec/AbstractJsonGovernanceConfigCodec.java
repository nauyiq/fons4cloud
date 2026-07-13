package com.fons.cloud.admin.domain.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** JSON 配置 Codec 基类，提供稳定键顺序、SHA-256 和通用语法校验。 */
public abstract class AbstractJsonGovernanceConfigCodec implements GovernanceConfigCodec {

    protected final ObjectMapper objectMapper;

    protected AbstractJsonGovernanceConfigCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public NormalizedConfig normalize(String content) {
        try {
            JsonNode canonical = canonicalize(objectMapper.readTree(content));
            String normalized = objectMapper.writeValueAsString(canonical);
            return new NormalizedConfig(normalized, sha256(normalized));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("governance config is not valid JSON", ex);
        }
    }

    @Override
    public List<ValidationIssue> validate(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(content);
            validateNode(root, issues);
        } catch (JsonProcessingException ex) {
            issues.add(new ValidationIssue("/", "INVALID_JSON", "配置不是合法 JSON"));
        }
        return List.copyOf(issues);
    }

    /** 子类只追加领域字段规则，不重复实现 JSON 解析。 */
    protected abstract void validateNode(JsonNode root, List<ValidationIssue> issues);

    private JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            node.properties().stream()
                    .sorted(Comparator.comparing(java.util.Map.Entry::getKey))
                    .forEach(entry -> sorted.set(entry.getKey(), canonicalize(entry.getValue())));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            node.forEach(item -> array.add(canonicalize(item)));
            return array;
        }
        return node;
    }

    private String sha256(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
