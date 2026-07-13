package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.api.enums.GovernanceReleaseType;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.common.base.exception.BizException;
import lombok.Getter;

import java.util.Date;
import java.util.Objects;

/**
 * 治理发布记录领域模型，封装发布、回滚执行记录的状态和失败摘要。
 */
@Getter
public class GovernanceRelease {

    private final AdminGovernanceRelease entity;

    private GovernanceRelease(AdminGovernanceRelease entity) {
        this.entity = Objects.requireNonNull(entity, "governance release entity must not be null");
    }

    /**
     * 创建运行中的发布或回滚记录。
     *
     * @param changeId    关联变更 ID
     * @param releaseNo   发布流水号
     * @param releaseType 发布类型
     * @param beforeHash  执行前目标内容 hash
     * @param operatorId  操作人
     * @return 发布记录领域对象
     */
    public static GovernanceRelease createRunning(Long changeId, String releaseNo, GovernanceReleaseType releaseType,
                                                  String beforeHash, String operatorId) {
        AdminGovernanceRelease entity = new AdminGovernanceRelease();
        entity.setChangeId(changeId);
        entity.setReleaseNo(releaseNo);
        entity.setReleaseType(releaseType.name());
        entity.setStatus(GovernanceReleaseStatus.RUNNING.name());
        entity.setBeforeHash(beforeHash);
        entity.setOperatorId(operatorId);
        entity.setStartedAt(new Date());
        return new GovernanceRelease(entity);
    }

    /**
     * 基于持久化实体恢复发布记录领域对象。
     *
     * @param entity 发布记录持久化实体
     * @return 发布记录领域对象
     */
    public static GovernanceRelease from(AdminGovernanceRelease entity) {
        return new GovernanceRelease(entity);
    }

    /**
     * 标记执行成功。
     *
     * @param afterHash 执行后目标内容 hash
     */
    public void markSuccess(String afterHash) {
        requireRunning();
        entity.setStatus(GovernanceReleaseStatus.SUCCESS.name());
        entity.setAfterHash(afterHash);
        entity.setFinishedAt(new Date());
    }

    /**
     * 标记执行失败，错误摘要会先做敏感信息脱敏。
     *
     * @param errorCode    错误码
     * @param errorMessage 错误摘要
     */
    public void markFailed(String errorCode, String errorMessage) {
        requireRunning();
        entity.setStatus(GovernanceReleaseStatus.FAILED.name());
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(GovernanceAudit.mask(errorMessage));
        entity.setFinishedAt(new Date());
    }

    /**
     * 标记发布前检测到外部漂移。
     *
     * @param currentHash 当前目标内容 hash
     */
    public void markDriftDetected(String currentHash) {
        requireRunning();
        entity.setStatus(GovernanceReleaseStatus.DRIFT_DETECTED.name());
        entity.setAfterHash(currentHash);
        entity.setErrorCode(AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getCode());
        entity.setErrorMessage(AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getMessage());
        entity.setFinishedAt(new Date());
    }

    /**
     * 标记发布后确认失败，后续人工核查可基于该状态继续处理。
     *
     * @param errorMessage 确认失败摘要
     */
    public void markPendingConfirm(String errorMessage) {
        markPendingConfirm(errorMessage, null);
    }

    public void markPendingConfirm(String errorMessage, String expectedAfterHash) {
        requireRunning();
        entity.setStatus(GovernanceReleaseStatus.PENDING_CONFIRM.name());
        entity.setAfterHash(expectedAfterHash);
        entity.setErrorCode(AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode());
        entity.setErrorMessage(GovernanceAudit.mask(errorMessage));
        entity.setFinishedAt(new Date());
    }

    /** 恢复任务确认外部目标已经写入成功。 */
    public void confirmSuccess(String afterHash) {
        requirePendingConfirm();
        entity.setStatus(GovernanceReleaseStatus.SUCCESS.name());
        entity.setAfterHash(afterHash);
        entity.setErrorCode(null);
        entity.setErrorMessage(null);
        entity.setFinishedAt(new Date());
    }

    /** 恢复任务确认目标仍为执行前状态。 */
    public void confirmNotApplied() {
        requirePendingConfirm();
        entity.setStatus(GovernanceReleaseStatus.FAILED.name());
        entity.setErrorCode(AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode());
        entity.setErrorMessage("目标仍为执行前版本，发布未生效");
        entity.setFinishedAt(new Date());
    }

    private void requireRunning() {
        if (!GovernanceReleaseStatus.RUNNING.name().equals(entity.getStatus())) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
    }

    private void requirePendingConfirm() {
        if (!GovernanceReleaseStatus.PENDING_CONFIRM.name().equals(entity.getStatus())) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
    }
}
