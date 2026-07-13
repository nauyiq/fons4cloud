package com.fons.cloud.admin.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.domain.codec.GovernanceConfigCodec;
import com.fons.cloud.admin.interfaces.rest.api.model.GovernanceDiffResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** 按 JSON 路径计算语义差异，并在服务端屏蔽敏感值。 */
@Service
public class GovernanceDiffService {

    private static final String SENSITIVE_CHANGED = "[SENSITIVE_CHANGED]";

    private final ObjectMapper objectMapper;

    public GovernanceDiffService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GovernanceDiffResponse diff(String beforeContent, String afterContent, GovernanceConfigCodec codec) {
        try {
            JsonNode before = objectMapper.readTree(codec.normalize(beforeContent).content());
            JsonNode after = objectMapper.readTree(codec.normalize(afterContent).content());
            List<GovernanceDiffResponse.DiffEntry> entries = new ArrayList<>();
            compare("", before, after, codec, entries);
            return new GovernanceDiffResponse(!entries.isEmpty(), List.copyOf(entries));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("normalized governance config is not valid JSON", ex);
        }
    }

    private void compare(String path, JsonNode before, JsonNode after, GovernanceConfigCodec codec,
                         List<GovernanceDiffResponse.DiffEntry> entries) {
        if (before == null || before.isMissingNode()) {
            add(path, GovernanceDiffResponse.Operation.ADD, null, after, codec, entries);
            return;
        }
        if (after == null || after.isMissingNode()) {
            add(path, GovernanceDiffResponse.Operation.REMOVE, before, null, codec, entries);
            return;
        }
        if (before.isObject() && after.isObject()) {
            Set<String> names = new TreeSet<>();
            before.fieldNames().forEachRemaining(names::add);
            after.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                compare(path + "/" + escape(name), before.path(name), after.path(name), codec, entries);
            }
            return;
        }
        if (before.isArray() && after.isArray()) {
            int length = Math.max(before.size(), after.size());
            for (int index = 0; index < length; index++) {
                compare(path + "/" + index, before.path(index), after.path(index), codec, entries);
            }
            return;
        }
        if (!before.equals(after)) {
            add(path, GovernanceDiffResponse.Operation.REPLACE, before, after, codec, entries);
        }
    }

    private void add(String path, GovernanceDiffResponse.Operation operation, JsonNode before, JsonNode after,
                     GovernanceConfigCodec codec, List<GovernanceDiffResponse.DiffEntry> entries) {
        boolean sensitive = codec.sensitivePath(path);
        entries.add(new GovernanceDiffResponse.DiffEntry(path.isEmpty() ? "/" : path, operation,
                sensitive ? sensitiveMarker(before) : value(before),
                sensitive ? sensitiveMarker(after) : value(after), sensitive));
    }

    private Object sensitiveMarker(JsonNode node) {
        return node == null || node.isMissingNode() ? null : SENSITIVE_CHANGED;
    }

    private Object value(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        return objectMapper.convertValue(node, Object.class);
    }

    private String escape(String segment) {
        return segment.replace("~", "~0").replace("/", "~1");
    }
}
