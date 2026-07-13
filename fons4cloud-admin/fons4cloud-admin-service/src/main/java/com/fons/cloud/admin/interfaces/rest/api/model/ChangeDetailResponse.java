package com.fons.cloud.admin.interfaces.rest.api.model;

import com.fons.cloud.admin.api.response.GovernanceChangeResponse;

import java.util.Date;
import java.util.List;
import java.util.Set;

/** 变更中心详情聚合，不返回快照配置正文。 */
public record ChangeDetailResponse(
        GovernanceChangeResponse change,
        List<ReleaseSummary> releases,
        List<SnapshotSummary> snapshots,
        Set<String> allowedActions) {

    public ChangeDetailResponse {
        releases = List.copyOf(releases);
        snapshots = List.copyOf(snapshots);
        allowedActions = Set.copyOf(allowedActions);
    }

    public record ReleaseSummary(Long id, String releaseNo, String releaseType, String status, String beforeHash,
                                 String afterHash, String errorCode, String errorMessage, Date startedAt, Date finishedAt) {
    }

    public record SnapshotSummary(Long id, String snapshotType, String contentHash, Date createdAt) {
    }
}
