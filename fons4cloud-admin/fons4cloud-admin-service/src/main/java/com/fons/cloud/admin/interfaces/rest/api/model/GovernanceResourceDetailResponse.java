package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.Set;

/** 外部当前态与 admin 登记信息的安全聚合视图。 */
public record GovernanceResourceDetailResponse(
        Long registeredResourceId,
        String domain,
        String resourceType,
        String resourceKey,
        String displayName,
        String targetRefSummary,
        String currentHash,
        String safeContent,
        String status,
        Set<String> allowedActions,
        boolean registered) {

    public GovernanceResourceDetailResponse {
        allowedActions = Set.copyOf(allowedActions);
    }
}
