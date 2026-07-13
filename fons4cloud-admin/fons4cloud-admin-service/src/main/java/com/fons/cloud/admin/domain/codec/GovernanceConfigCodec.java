package com.fons.cloud.admin.domain.codec;

import com.fons.cloud.admin.api.enums.GovernanceDomain;

import java.util.List;
import java.util.Set;

/**
 * 治理配置语义编解码端口。
 *
 * <p>所有 typed 表单和高级 JSON 最终必须经过服务端 Codec 规范化与校验，前端不得自行决定 hash 和敏感路径。</p>
 */
public interface GovernanceConfigCodec {

    GovernanceDomain domain();

    Set<String> resourceTypes();

    NormalizedConfig normalize(String content);

    List<ValidationIssue> validate(String content);

    default boolean sensitivePath(String path) {
        String normalized = path == null ? "" : path.toLowerCase();
        return normalized.contains("secret") || normalized.contains("password")
                || normalized.contains("token") || normalized.contains("credential");
    }

    record NormalizedConfig(String content, String contentHash) {
    }

    record ValidationIssue(String path, String code, String message) {
    }
}
