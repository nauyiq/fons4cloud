package com.fons.cloud.admin.api.enums;

/**
 * 治理变更生命周期状态。
 */
public enum GovernanceChangeStatus {
    DRAFT,
    VALIDATING,
    VALIDATED,
    VALIDATION_FAILED,
    PUBLISHING,
    PUBLISHED,
    PUBLISH_FAILED,
    DRIFT_DETECTED,
    ROLLBACKING,
    ROLLED_BACK,
    ROLLBACK_FAILED
}
