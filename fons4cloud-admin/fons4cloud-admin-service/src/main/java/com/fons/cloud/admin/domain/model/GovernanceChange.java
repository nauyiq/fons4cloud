package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.common.base.exception.BizException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * 治理变更领域模型，集中封装草稿、校验、发布、漂移和回滚状态流转规则。
 */
@Getter
public class GovernanceChange {

    private final AdminGovernanceChange entity;

    private GovernanceChange(AdminGovernanceChange entity) {
        this.entity = Objects.requireNonNull(entity, "governance change entity must not be null");
    }

    /**
     * 基于持久化实体恢复治理变更领域对象。
     *
     * @param entity 治理变更持久化实体
     * @return 治理变更领域对象
     */
    public static GovernanceChange from(AdminGovernanceChange entity) {
        return new GovernanceChange(entity);
    }

    /**
     * 创建处于 DRAFT 状态的治理草稿实体。
     *
     * @param resourceId  治理资源 ID
     * @param changeNo    变更单号
     * @param changeType  变更类型
     * @param baseHash    创建草稿时的目标配置基线 hash
     * @param content     期望发布的配置正文
     * @param contentHash 配置正文 hash，为空时按正文计算 SHA-256
     * @param description 变更说明
     * @param operatorId  创建人
     * @return 治理变更领域对象
     */
    public static GovernanceChange createDraft(Long resourceId, String changeNo, GovernanceChangeType changeType,
                                               String baseHash, String content, String contentHash,
                                               String description, String operatorId) {
        AdminGovernanceChange entity = new AdminGovernanceChange();
        entity.setResourceId(resourceId);
        entity.setChangeNo(changeNo);
        entity.setChangeType((changeType == null ? GovernanceChangeType.UPDATE : changeType).name());
        entity.setStatus(GovernanceChangeStatus.DRAFT.name());
        entity.setBaseHash(baseHash);
        entity.setContent(content);
        entity.setContentHash(StringUtils.defaultIfBlank(contentHash, hash(content)));
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDescription(description);
        return new GovernanceChange(entity);
    }

    /**
     * 创建回滚变更实体。回滚以历史快照为期望内容生成新变更，不覆盖原发布历史。
     *
     * @param sourceSnapshot 回滚来源快照
     * @param changeNo       回滚变更单号
     * @param operatorId     发起人
     * @param reason         回滚原因
     * @return 已通过内容校验的回滚变更
     */
    public static GovernanceChange createRollback(AdminGovernanceSnapshot sourceSnapshot, String changeNo,
                                                  String operatorId, String reason) {
        AdminGovernanceChange entity = new AdminGovernanceChange();
        entity.setResourceId(sourceSnapshot.getResourceId());
        entity.setChangeNo(changeNo);
        entity.setChangeType(GovernanceChangeType.ROLLBACK.name());
        entity.setStatus(GovernanceChangeStatus.VALIDATED.name());
        entity.setBaseHash(sourceSnapshot.getContentHash());
        entity.setContent(sourceSnapshot.getContent());
        entity.setContentHash(sourceSnapshot.getContentHash());
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        entity.setDescription(reason);
        entity.setValidationResult("{\"rollbackSourceSnapshotId\":" + sourceSnapshot.getId() + "}");
        return new GovernanceChange(entity);
    }

    /**
     * 进入校验中状态。
     *
     * @param operatorId 操作人
     */
    public void startValidation(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.DRAFT,
                GovernanceChangeStatus.VALIDATION_FAILED);
        transitionTo(GovernanceChangeStatus.VALIDATING, operatorId);
    }

    /**
     * 标记校验通过。
     *
     * @param normalizedContentHash 标准化内容 hash
     * @param validationResult      校验结果 JSON
     * @param operatorId            操作人
     */
    public void markValidated(String normalizedContentHash, String validationResult, String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.VALIDATING);
        entity.setContentHash(StringUtils.defaultIfBlank(normalizedContentHash, entity.getContentHash()));
        entity.setValidationResult(validationResult);
        transitionTo(GovernanceChangeStatus.VALIDATED, operatorId);
    }

    /**
     * 标记校验失败。
     *
     * @param validationResult 校验失败结果 JSON
     * @param operatorId       操作人
     */
    public void markValidationFailed(String validationResult, String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.VALIDATING);
        entity.setValidationResult(validationResult);
        transitionTo(GovernanceChangeStatus.VALIDATION_FAILED, operatorId);
    }

    /**
     * 进入发布中状态，仅已校验通过的普通变更可发布。
     *
     * @param operatorId 操作人
     */
    public void startPublish(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.VALIDATED);
        transitionTo(GovernanceChangeStatus.PUBLISHING, operatorId);
    }

    /**
     * 标记发布成功。
     *
     * @param operatorId 操作人
     */
    public void markPublished(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.PUBLISHING);
        transitionTo(GovernanceChangeStatus.PUBLISHED, operatorId);
    }

    /**
     * 标记发布失败。
     *
     * @param operatorId 操作人
     */
    public void markPublishFailed(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.PUBLISHING);
        transitionTo(GovernanceChangeStatus.PUBLISH_FAILED, operatorId);
    }

    /**
     * 标记发布前发现外部漂移。
     *
     * @param operatorId 操作人
     */
    public void markDriftDetected(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.PUBLISHING);
        transitionTo(GovernanceChangeStatus.DRIFT_DETECTED, operatorId);
    }

    /**
     * 进入回滚中状态。历史已发布变更和新生成的回滚变更都允许进入回滚发布流程。
     *
     * @param operatorId 操作人
     */
    public void startRollback(String operatorId) {
        GovernanceChangeStatus status = status();
        boolean rollbackDraft = GovernanceChangeType.ROLLBACK.name().equals(entity.getChangeType())
                && GovernanceChangeStatus.VALIDATED == status;
        if (GovernanceChangeStatus.PUBLISHED != status && !rollbackDraft) {
            throw new BizException(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        transitionTo(GovernanceChangeStatus.ROLLBACKING, operatorId);
    }

    /**
     * 标记回滚成功。
     *
     * @param operatorId 操作人
     */
    public void markRolledBack(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.ROLLBACKING);
        transitionTo(GovernanceChangeStatus.ROLLED_BACK, operatorId);
    }

    /**
     * 标记回滚失败。
     *
     * @param operatorId 操作人
     */
    public void markRollbackFailed(String operatorId) {
        requireStatus(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE, GovernanceChangeStatus.ROLLBACKING);
        transitionTo(GovernanceChangeStatus.ROLLBACK_FAILED, operatorId);
    }

    private GovernanceChangeStatus status() {
        return GovernanceChangeStatus.valueOf(entity.getStatus());
    }

    private void requireStatus(AdminResultCode resultCode, GovernanceChangeStatus... expectedStatuses) {
        GovernanceChangeStatus current = status();
        boolean matched = Arrays.stream(expectedStatuses).anyMatch(expected -> expected == current);
        if (!matched) {
            throw new BizException(resultCode);
        }
    }

    private void transitionTo(GovernanceChangeStatus targetStatus, String operatorId) {
        entity.setStatus(targetStatus.name());
        entity.setUpdatedBy(operatorId);
    }

    private static String hash(String content) {
        if (content == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
