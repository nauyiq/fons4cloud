package com.fons.cloud.admin.api.enums;

/**
 * 发布执行状态。
 */
public enum GovernanceReleaseStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    DRIFT_DETECTED,
    PENDING_CONFIRM
}
