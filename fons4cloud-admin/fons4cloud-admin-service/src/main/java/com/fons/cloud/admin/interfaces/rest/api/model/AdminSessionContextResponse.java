package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.Map;
import java.util.Set;

/** 当前管理员、环境和非敏感依赖状态。 */
public record AdminSessionContextResponse(
        Long userId,
        String username,
        String environmentName,
        Set<String> permissions,
        Map<String, DependencyState> dependencies) {

    public enum DependencyState {
        UP,
        DOWN,
        UNKNOWN
    }
}
