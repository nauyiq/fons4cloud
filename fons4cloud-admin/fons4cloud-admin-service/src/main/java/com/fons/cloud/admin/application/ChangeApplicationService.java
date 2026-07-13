package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceAuditResult;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.api.enums.GovernanceReleaseType;
import com.fons.cloud.admin.api.enums.GovernanceSnapshotType;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceReleaseMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceSnapshotMapper;
import com.fons.cloud.admin.domain.model.GovernanceAudit;
import com.fons.cloud.admin.domain.model.GovernanceChange;
import com.fons.cloud.admin.domain.model.GovernanceRelease;
import com.fons.cloud.admin.domain.model.GovernanceSnapshot;
import com.fons.cloud.admin.infrastructure.converter.GovernanceChangeConverter;
import com.fons.cloud.admin.interfaces.rest.api.model.ChangeDetailResponse;
import com.fons.cloud.admin.interfaces.rest.api.model.PageResponse;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 统一治理变更应用服务，负责编排变更状态、发布记录、快照和审计。
 */
@Service
@RequiredArgsConstructor
public class ChangeApplicationService {

    private static final String DOMAIN_CHANGES = "changes";
    private static final String OPERATION_VALIDATE = "VALIDATE";
    private static final String OPERATION_PUBLISH = "PUBLISH";
    private static final String OPERATION_DRIFT = "DRIFT_DETECTED";
    private static final String OPERATION_ROLLBACK = "ROLLBACK";

    private final AdminGovernanceChangeMapper changeMapper;
    private final AdminGovernanceReleaseMapper releaseMapper;
    private final AdminGovernanceSnapshotMapper snapshotMapper;
    private final AuditApplicationService auditApplicationService;

    /**
     * 查询治理变更列表。当前版本按资源 ID 和状态做轻量过滤，分页后续由通用查询模型统一补齐。
     *
     * @param resourceId 治理资源 ID，可为空
     * @param status     变更状态，可为空
     * @return 治理变更列表
     */
    public R<List<GovernanceChangeResponse>> query(Long resourceId, String status) {
        LambdaQueryWrapper<AdminGovernanceChange> wrapper = new LambdaQueryWrapper<>();
        if (resourceId != null) {
            wrapper.eq(AdminGovernanceChange::getResourceId, resourceId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(AdminGovernanceChange::getStatus, status);
        }
        wrapper.orderByDesc(AdminGovernanceChange::getId);
        wrapper.last("LIMIT 100");
        return R.ok(changeMapper.selectList(wrapper).stream()
                .map(GovernanceChangeConverter.CONVERTER::mapToResponse)
                .toList());
    }

    /** 强制上限的变更分页查询。 */
    public R<PageResponse<GovernanceChangeResponse>> queryPage(Long resourceId, String status, int offset, int limit) {
        int safeLimit = Math.min(100, Math.max(1, limit));
        int safeOffset = Math.max(0, offset);
        LambdaQueryWrapper<AdminGovernanceChange> wrapper = new LambdaQueryWrapper<>();
        if (resourceId != null) {
            wrapper.eq(AdminGovernanceChange::getResourceId, resourceId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(AdminGovernanceChange::getStatus, status);
        }
        wrapper.orderByDesc(AdminGovernanceChange::getId);
        Page<AdminGovernanceChange> page = changeMapper.selectPage(
                new Page<>((long) safeOffset / safeLimit + 1L, safeLimit), wrapper);
        return R.ok(new PageResponse<>(page.getRecords().stream()
                .map(GovernanceChangeConverter.CONVERTER::mapToResponse).toList(), page.getTotal(), safeOffset,
                safeLimit));
    }

    /**
     * 查询单个治理变更详情。
     *
     * @param changeId 治理变更 ID
     * @return 治理变更详情
     */
    public R<GovernanceChangeResponse> getById(Long changeId) {
        AdminGovernanceChange change = changeMapper.selectById(changeId);
        return change == null ? R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE)
                : R.ok(GovernanceChangeConverter.CONVERTER.mapToResponse(change));
    }

    /** 查询变更、发布和快照摘要，快照正文不向列表页面暴露。 */
    public R<ChangeDetailResponse> getDetail(Long changeId) {
        AdminGovernanceChange change = changeMapper.selectById(changeId);
        if (change == null) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        List<ChangeDetailResponse.ReleaseSummary> releases = releaseMapper.selectList(
                        new LambdaQueryWrapper<AdminGovernanceRelease>()
                                .eq(AdminGovernanceRelease::getChangeId, changeId)
                                .orderByDesc(AdminGovernanceRelease::getId))
                .stream().map(release -> new ChangeDetailResponse.ReleaseSummary(release.getId(),
                        release.getReleaseNo(), release.getReleaseType(), release.getStatus(), release.getBeforeHash(),
                        release.getAfterHash(), release.getErrorCode(), release.getErrorMessage(),
                        release.getStartedAt(), release.getFinishedAt())).toList();
        List<ChangeDetailResponse.SnapshotSummary> snapshots = snapshotMapper.selectList(
                        new LambdaQueryWrapper<AdminGovernanceSnapshot>()
                                .eq(AdminGovernanceSnapshot::getChangeId, changeId)
                                .orderByDesc(AdminGovernanceSnapshot::getId))
                .stream().map(snapshot -> new ChangeDetailResponse.SnapshotSummary(snapshot.getId(),
                        snapshot.getSnapshotType(), snapshot.getContentHash(), snapshot.getCreated())).toList();
        return R.ok(new ChangeDetailResponse(GovernanceChangeConverter.CONVERTER.mapToResponse(change), releases,
                snapshots, allowedActions(change)));
    }

    private Set<String> allowedActions(AdminGovernanceChange change) {
        GovernanceChangeStatus status = GovernanceChangeStatus.valueOf(change.getStatus());
        return switch (status) {
            case DRAFT, VALIDATION_FAILED -> Set.of("UPDATE", "VALIDATE");
            case VALIDATED -> Set.of("PUBLISH");
            case PUBLISHED -> Set.of("ROLLBACK");
            case PUBLISHING, ROLLBACKING -> Set.of("QUERY_STATUS");
            case DRIFT_DETECTED -> Set.of("UPDATE", "REBASE");
            case PUBLISH_FAILED, ROLLBACK_FAILED -> Set.of("VIEW_ERROR");
            case VALIDATING, ROLLED_BACK -> Set.of();
        };
    }

    /** 更新 DRAFT 或 VALIDATION_FAILED 草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernanceChangeResponse> updateDraft(Long changeId, String content, String description,
                                                   String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.updateDraft(content, description, operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), "DRAFT_UPDATE", operatorId, GovernanceAuditResult.SUCCESS.name(),
                "draft updated, contentHash=" + change.getEntity().getContentHash(), null, null);
        return R.ok(GovernanceChangeConverter.CONVERTER.mapToResponse(change.getEntity()));
    }

    /**
     * 创建治理草稿。资源定位、内容规范化和目标系统读取由后续适配器编排完成后传入。
     *
     * @param resourceId   治理资源 ID
     * @param changeNo     变更单号
     * @param changeType   变更类型
     * @param baseHash     创建草稿时目标配置基线 hash
     * @param content      期望发布的配置正文
     * @param contentHash  配置正文 hash
     * @param description  变更说明
     * @param operatorId   操作人
     * @return 新建草稿实体
     */
    @Transactional(rollbackFor = Exception.class)
    public R<AdminGovernanceChange> createDraft(Long resourceId, String changeNo, GovernanceChangeType changeType,
                                                String baseHash, String content, String contentHash,
                                                String description, String operatorId) {
        GovernanceChange change = GovernanceChange.createDraft(resourceId, changeNo, changeType, baseHash, content,
                contentHash, description, operatorId);
        changeMapper.insert(change.getEntity());
        recordAudit(change.getEntity(), "DRAFT_CREATE", operatorId, GovernanceAuditResult.SUCCESS.name(),
                "draft created, changeNo=" + changeNo + ", contentHash=" + change.getEntity().getContentHash(),
                null, null);
        return R.ok(change.getEntity());
    }

    /**
     * 标记草稿校验成功。
     *
     * @param changeId              变更 ID
     * @param normalizedContentHash 标准化内容 hash
     * @param validationResult      校验结果 JSON
     * @param operatorId            操作人
     * @return 是否处理成功
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> validateSucceeded(Long changeId, String normalizedContentHash, String validationResult,
                                        String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.startValidation(operatorId);
        change.markValidated(normalizedContentHash, validationResult, operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_VALIDATE, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "validate passed, contentHash=" + normalizedContentHash, null, null);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 标记草稿校验失败，失败状态会阻止后续发布。
     *
     * @param changeId         变更 ID
     * @param validationResult 校验失败结果 JSON
     * @param operatorId       操作人
     * @return 是否处理成功
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> validateFailed(Long changeId, String validationResult, String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.startValidation(operatorId);
        change.markValidationFailed(validationResult, operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_VALIDATE, operatorId, GovernanceAuditResult.FAILED.name(),
                "validate failed", AdminResultCode.ADMIN_VALIDATION_FAILED.getCode(), validationResult);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 在外部写入前原子占用变更并创建 RUNNING 发布记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public R<AdminGovernanceRelease> beginPublishExecution(Long changeId, String releaseNo, String beforeHash,
                                                           String operatorId) {
        AdminGovernanceChange entity = changeMapper.selectById(changeId);
        if (entity == null || !GovernanceChangeStatus.VALIDATED.name().equals(entity.getStatus())) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        ensureNoRunningChange(entity.getResourceId(), entity.getId());
        int updated = changeMapper.update(null, new LambdaUpdateWrapper<AdminGovernanceChange>()
                .eq(AdminGovernanceChange::getId, entity.getId())
                .eq(AdminGovernanceChange::getStatus, GovernanceChangeStatus.VALIDATED.name())
                .eq(AdminGovernanceChange::getVersion, entity.getVersion())
                .set(AdminGovernanceChange::getStatus, GovernanceChangeStatus.PUBLISHING.name())
                .set(AdminGovernanceChange::getUpdatedBy, operatorId)
                .set(AdminGovernanceChange::getVersion, entity.getVersion() + 1));
        if (updated != 1) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.PUBLISH, beforeHash,
                operatorId);
        return R.ok(release.getEntity());
    }

    /** 在外部回滚写入前原子占用回滚变更并创建 RUNNING 记录。 */
    @Transactional(rollbackFor = Exception.class)
    public R<AdminGovernanceRelease> beginRollbackExecution(Long changeId, String releaseNo, String beforeHash,
                                                            String operatorId) {
        AdminGovernanceChange entity = changeMapper.selectById(changeId);
        if (entity == null || !GovernanceChangeStatus.VALIDATED.name().equals(entity.getStatus())
                || !GovernanceChangeType.ROLLBACK.name().equals(entity.getChangeType())) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        ensureNoRunningChange(entity.getResourceId(), entity.getId());
        int updated = changeMapper.update(null, new LambdaUpdateWrapper<AdminGovernanceChange>()
                .eq(AdminGovernanceChange::getId, entity.getId())
                .eq(AdminGovernanceChange::getStatus, GovernanceChangeStatus.VALIDATED.name())
                .eq(AdminGovernanceChange::getVersion, entity.getVersion())
                .set(AdminGovernanceChange::getStatus, GovernanceChangeStatus.ROLLBACKING.name())
                .set(AdminGovernanceChange::getUpdatedBy, operatorId)
                .set(AdminGovernanceChange::getVersion, entity.getVersion() + 1));
        if (updated != 1) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.ROLLBACK, beforeHash,
                operatorId);
        return R.ok(release.getEntity());
    }

    /** 发布后回读确认成功。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> completePublishExecution(Long changeId, Long releaseId, String beforeContent,
                                                                String beforeHash, String afterContent, String afterHash,
                                                                String operatorId, String effectiveHint) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.PUBLISHING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.BEFORE, beforeContent, beforeHash);
        release.markSuccess(afterHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.AFTER, afterContent, afterHash);
        change.markPublished(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_PUBLISH, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "publish confirmed, releaseNo=" + release.getEntity().getReleaseNo() + ", afterHash=" + afterHash,
                null, null);
        return R.ok(toPublishResult(release.getEntity(), effectiveHint));
    }

    /** 外部目标明确返回失败。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> failPublishExecution(Long changeId, Long releaseId, String errorCode,
                                                            String errorMessage, String operatorId) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.PUBLISHING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        release.markFailed(errorCode, errorMessage);
        releaseMapper.updateById(release.getEntity());
        change.markPublishFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_PUBLISH, operatorId, GovernanceAuditResult.FAILED.name(),
                "publish failed, releaseNo=" + release.getEntity().getReleaseNo(), errorCode, errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 发布前发现目标已漂移。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> driftPublishExecution(Long changeId, Long releaseId, String currentContent,
                                                             String currentHash, String operatorId) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.PUBLISHING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        release.markDriftDetected(currentHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.DRIFT_CURRENT, currentContent, currentHash);
        change.markDriftDetected(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_DRIFT, operatorId, GovernanceAuditResult.FAILED.name(),
                "target drift detected, releaseNo=" + release.getEntity().getReleaseNo() + ", currentHash=" + currentHash,
                AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getCode(), AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getMessage());
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 写入结果无法回读确认，发布记录待确认且变更继续保持 PUBLISHING。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> pendingConfirmExecution(Long changeId, Long releaseId, String beforeContent,
                                                               String beforeHash, String expectedAfterHash, String errorMessage,
                                                               String operatorId) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.PUBLISHING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.BEFORE, beforeContent, beforeHash);
        release.markPendingConfirm(errorMessage, expectedAfterHash);
        releaseMapper.updateById(release.getEntity());
        recordAudit(change.getEntity(), "PUBLISH_PENDING_CONFIRM", operatorId, GovernanceAuditResult.FAILED.name(),
                "publish result requires target readback, releaseNo=" + release.getEntity().getReleaseNo(),
                AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode(), errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 回滚后回读确认成功。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> completeRollbackExecution(Long changeId, Long releaseId, String beforeContent,
                                                                 String beforeHash, String afterContent, String afterHash,
                                                                 String operatorId, String effectiveHint) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.ROLLBACKING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.BEFORE, beforeContent, beforeHash);
        release.markSuccess(afterHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.AFTER, afterContent, afterHash);
        change.markRolledBack(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_ROLLBACK, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "rollback confirmed, releaseNo=" + release.getEntity().getReleaseNo() + ", afterHash=" + afterHash,
                null, null);
        return R.ok(toPublishResult(release.getEntity(), effectiveHint));
    }

    /** 回滚目标明确失败。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> failRollbackExecution(Long changeId, Long releaseId, String errorCode,
                                                             String errorMessage, String operatorId) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.ROLLBACKING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        release.markFailed(errorCode, errorMessage);
        releaseMapper.updateById(release.getEntity());
        change.markRollbackFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_ROLLBACK, operatorId, GovernanceAuditResult.FAILED.name(),
                "rollback failed, releaseNo=" + release.getEntity().getReleaseNo(), errorCode, errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 回滚写入后无法确认，变更保持 ROLLBACKING。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> pendingRollbackConfirmExecution(Long changeId, Long releaseId,
                                                                       String beforeContent, String beforeHash,
                                                                       String expectedAfterHash, String errorMessage,
                                                                       String operatorId) {
        GovernanceChange change = requireExecutingChange(changeId, GovernanceChangeStatus.ROLLBACKING);
        GovernanceRelease release = requireRunningRelease(releaseId);
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.BEFORE, beforeContent, beforeHash);
        release.markPendingConfirm(errorMessage, expectedAfterHash);
        releaseMapper.updateById(release.getEntity());
        recordAudit(change.getEntity(), "ROLLBACK_PENDING_CONFIRM", operatorId, GovernanceAuditResult.FAILED.name(),
                "rollback result requires target readback, releaseNo=" + release.getEntity().getReleaseNo(),
                AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode(), errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 恢复任务确认待确认发布已经生效。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> confirmRecoveredSuccess(Long releaseId, String currentContent,
                                                               String currentHash, String operatorId) {
        AdminGovernanceRelease releaseEntity = releaseMapper.selectById(releaseId);
        GovernanceRelease release = GovernanceRelease.from(releaseEntity);
        GovernanceChange change = requireExecutingChange(releaseEntity.getChangeId(), GovernanceChangeStatus.PUBLISHING);
        release.confirmSuccess(currentHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.AFTER, currentContent, currentHash);
        change.markPublished(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), "PUBLISH_RECOVERED", operatorId, GovernanceAuditResult.SUCCESS.name(),
                "pending publish confirmed, releaseNo=" + releaseEntity.getReleaseNo(), null, null);
        return R.ok(toPublishResult(release.getEntity(), "目标回读已确认发布成功"));
    }

    /** 恢复任务确认外部目标仍为执行前版本。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> confirmRecoveredNotApplied(Long releaseId, String operatorId) {
        AdminGovernanceRelease releaseEntity = releaseMapper.selectById(releaseId);
        GovernanceRelease release = GovernanceRelease.from(releaseEntity);
        GovernanceChange change = requireExecutingChange(releaseEntity.getChangeId(), GovernanceChangeStatus.PUBLISHING);
        release.confirmNotApplied();
        releaseMapper.updateById(release.getEntity());
        change.markPublishFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), "PUBLISH_RECOVERED", operatorId, GovernanceAuditResult.FAILED.name(),
                "pending publish not applied, releaseNo=" + releaseEntity.getReleaseNo(),
                AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode(), "目标仍为执行前版本");
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /** 恢复任务确认待确认回滚已经生效。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> confirmRecoveredRollbackSuccess(Long releaseId, String currentContent,
                                                                       String currentHash, String operatorId) {
        AdminGovernanceRelease releaseEntity = releaseMapper.selectById(releaseId);
        GovernanceRelease release = GovernanceRelease.from(releaseEntity);
        GovernanceChange change = requireExecutingChange(releaseEntity.getChangeId(), GovernanceChangeStatus.ROLLBACKING);
        release.confirmSuccess(currentHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), releaseId, GovernanceSnapshotType.AFTER, currentContent, currentHash);
        change.markRolledBack(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), "ROLLBACK_RECOVERED", operatorId, GovernanceAuditResult.SUCCESS.name(),
                "pending rollback confirmed, releaseNo=" + releaseEntity.getReleaseNo(), null, null);
        return R.ok(toPublishResult(release.getEntity(), "目标回读已确认回滚成功"));
    }

    /** 恢复任务确认待确认回滚未生效。 */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> confirmRecoveredRollbackNotApplied(Long releaseId, String operatorId) {
        AdminGovernanceRelease releaseEntity = releaseMapper.selectById(releaseId);
        GovernanceRelease release = GovernanceRelease.from(releaseEntity);
        GovernanceChange change = requireExecutingChange(releaseEntity.getChangeId(), GovernanceChangeStatus.ROLLBACKING);
        release.confirmNotApplied();
        releaseMapper.updateById(release.getEntity());
        change.markRollbackFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), "ROLLBACK_RECOVERED", operatorId, GovernanceAuditResult.FAILED.name(),
                "pending rollback not applied, releaseNo=" + releaseEntity.getReleaseNo(),
                AdminResultCode.ADMIN_PUBLISH_CONFIRM_FAILED.getCode(), "目标仍为执行前版本");
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    private GovernanceChange requireExecutingChange(Long changeId, GovernanceChangeStatus expected) {
        GovernanceChange change = loadChange(changeId);
        if (change.status() != expected) {
            throw new BizException(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        return change;
    }

    private GovernanceRelease requireRunningRelease(Long releaseId) {
        AdminGovernanceRelease entity = releaseMapper.selectById(releaseId);
        if (entity == null || !GovernanceReleaseStatus.RUNNING.name().equals(entity.getStatus())) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        return GovernanceRelease.from(entity);
    }

    /**
     * 标记发布成功，并生成发布前、发布后快照。
     *
     * @param changeId      变更 ID
     * @param releaseNo     发布流水号
     * @param beforeContent 发布前目标内容
     * @param beforeHash    发布前目标内容 hash
     * @param afterContent  发布后目标内容
     * @param afterHash     发布后目标内容 hash
     * @param operatorId    操作人
     * @param effectiveHint 运行时生效提示
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> publishSucceeded(Long changeId, String releaseNo, String beforeContent,
                                                       String beforeHash, String afterContent, String afterHash,
                                                       String operatorId, String effectiveHint) {
        GovernanceChange change = loadChange(changeId);
        change.startPublish(operatorId);
        ensureNoRunningChange(change.getEntity().getResourceId());
        changeMapper.updateById(change.getEntity());

        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.PUBLISH, beforeHash,
                operatorId);
        saveSnapshot(change.getEntity(), release.getEntity().getId(), GovernanceSnapshotType.BEFORE,
                beforeContent, beforeHash);
        release.markSuccess(afterHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), release.getEntity().getId(), GovernanceSnapshotType.AFTER,
                afterContent, afterHash);

        change.markPublished(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_PUBLISH, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "publish success, releaseNo=" + releaseNo + ", afterHash=" + afterHash, null, null);
        return R.ok(toPublishResult(release.getEntity(), effectiveHint));
    }

    /**
     * 标记发布失败，并保留发布前 hash 和失败摘要。
     *
     * @param changeId     变更 ID
     * @param releaseNo    发布流水号
     * @param beforeHash   发布前目标内容 hash
     * @param errorCode    错误码
     * @param errorMessage 错误摘要
     * @param operatorId   操作人
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> publishFailed(Long changeId, String releaseNo, String beforeHash,
                                                    String errorCode, String errorMessage, String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.startPublish(operatorId);
        ensureNoRunningChange(change.getEntity().getResourceId());
        changeMapper.updateById(change.getEntity());

        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.PUBLISH, beforeHash,
                operatorId);
        release.markFailed(errorCode, errorMessage);
        releaseMapper.updateById(release.getEntity());

        change.markPublishFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_PUBLISH, operatorId, GovernanceAuditResult.FAILED.name(),
                "publish failed, releaseNo=" + releaseNo, errorCode, errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /**
     * 标记发布前发现目标配置漂移，并保存当前目标快照。
     *
     * @param changeId       变更 ID
     * @param releaseNo      发布流水号
     * @param currentContent 当前目标内容
     * @param currentHash    当前目标内容 hash
     * @param operatorId     操作人
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> detectDrift(Long changeId, String releaseNo, String currentContent,
                                                  String currentHash, String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.startPublish(operatorId);
        ensureNoRunningChange(change.getEntity().getResourceId());
        changeMapper.updateById(change.getEntity());

        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.PUBLISH,
                change.getEntity().getBaseHash(), operatorId);
        release.markDriftDetected(currentHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), release.getEntity().getId(), GovernanceSnapshotType.DRIFT_CURRENT,
                currentContent, currentHash);

        change.markDriftDetected(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_DRIFT, operatorId, GovernanceAuditResult.FAILED.name(),
                "target drift detected, releaseNo=" + releaseNo + ", currentHash=" + currentHash,
                AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getCode(),
                AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getMessage());
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    /**
     * 基于历史快照创建新的回滚变更，并保存回滚来源快照。
     *
     * @param snapshotId  回滚来源快照 ID
     * @param changeNo    新回滚变更单号
     * @param operatorId  操作人
     * @param reason      回滚原因
     * @return 新回滚变更实体
     */
    @Transactional(rollbackFor = Exception.class)
    public R<AdminGovernanceChange> createRollbackChange(Long snapshotId, String changeNo, String operatorId,
                                                         String reason) {
        AdminGovernanceSnapshot sourceSnapshot = snapshotMapper.selectById(snapshotId);
        if (sourceSnapshot == null) {
            return R.failed(AdminResultCode.ADMIN_ROLLBACK_UNSUPPORTED);
        }
        GovernanceChange rollbackChange = GovernanceChange.createRollback(sourceSnapshot, changeNo, operatorId, reason);
        changeMapper.insert(rollbackChange.getEntity());
        saveSnapshot(rollbackChange.getEntity(), null, GovernanceSnapshotType.ROLLBACK_SOURCE,
                sourceSnapshot.getContent(), sourceSnapshot.getContentHash());
        recordAudit(rollbackChange.getEntity(), OPERATION_ROLLBACK, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "rollback change created, sourceSnapshotId=" + snapshotId, null, null);
        return R.ok(rollbackChange.getEntity());
    }

    /**
     * 标记回滚发布成功，并生成回滚前、回滚后快照。
     *
     * @param changeId      回滚变更 ID
     * @param releaseNo     回滚发布流水号
     * @param beforeContent 回滚前目标内容
     * @param beforeHash    回滚前目标内容 hash
     * @param afterContent  回滚后目标内容
     * @param afterHash     回滚后目标内容 hash
     * @param operatorId    操作人
     * @param effectiveHint 运行时生效提示
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> rollbackSucceeded(Long changeId, String releaseNo, String beforeContent,
                                                        String beforeHash, String afterContent, String afterHash,
                                                        String operatorId, String effectiveHint) {
        GovernanceChange change = loadChange(changeId);
        change.startRollback(operatorId);
        ensureNoRunningChange(change.getEntity().getResourceId());
        changeMapper.updateById(change.getEntity());

        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.ROLLBACK, beforeHash,
                operatorId);
        saveSnapshot(change.getEntity(), release.getEntity().getId(), GovernanceSnapshotType.BEFORE,
                beforeContent, beforeHash);
        release.markSuccess(afterHash);
        releaseMapper.updateById(release.getEntity());
        saveSnapshot(change.getEntity(), release.getEntity().getId(), GovernanceSnapshotType.AFTER,
                afterContent, afterHash);

        change.markRolledBack(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_ROLLBACK, operatorId, GovernanceAuditResult.SUCCESS.name(),
                "rollback success, releaseNo=" + releaseNo + ", afterHash=" + afterHash, null, null);
        return R.ok(toPublishResult(release.getEntity(), effectiveHint));
    }

    /**
     * 标记回滚发布失败。
     *
     * @param changeId     回滚变更 ID
     * @param releaseNo    回滚发布流水号
     * @param beforeHash   回滚前目标内容 hash
     * @param errorCode    错误码
     * @param errorMessage 错误摘要
     * @param operatorId   操作人
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public R<GovernancePublishResult> rollbackFailed(Long changeId, String releaseNo, String beforeHash,
                                                     String errorCode, String errorMessage, String operatorId) {
        GovernanceChange change = loadChange(changeId);
        change.startRollback(operatorId);
        ensureNoRunningChange(change.getEntity().getResourceId());
        changeMapper.updateById(change.getEntity());

        GovernanceRelease release = createRelease(changeId, releaseNo, GovernanceReleaseType.ROLLBACK, beforeHash,
                operatorId);
        release.markFailed(errorCode, errorMessage);
        releaseMapper.updateById(release.getEntity());

        change.markRollbackFailed(operatorId);
        changeMapper.updateById(change.getEntity());
        recordAudit(change.getEntity(), OPERATION_ROLLBACK, operatorId, GovernanceAuditResult.FAILED.name(),
                "rollback failed, releaseNo=" + releaseNo, errorCode, errorMessage);
        return R.ok(toPublishResult(release.getEntity(), null));
    }

    private GovernanceChange loadChange(Long changeId) {
        AdminGovernanceChange entity = changeMapper.selectById(changeId);
        if (entity == null) {
            throw new BizException(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        return GovernanceChange.from(entity);
    }

    private GovernanceRelease createRelease(Long changeId, String releaseNo, GovernanceReleaseType releaseType,
                                            String beforeHash, String operatorId) {
        GovernanceRelease release = GovernanceRelease.createRunning(changeId, releaseNo, releaseType, beforeHash,
                operatorId);
        releaseMapper.insert(release.getEntity());
        return release;
    }

    private void saveSnapshot(AdminGovernanceChange change, Long releaseId, GovernanceSnapshotType snapshotType,
                              String content, String contentHash) {
        AdminGovernanceSnapshot snapshot = GovernanceSnapshot.create(change.getResourceId(), change.getId(), releaseId,
                snapshotType, content, contentHash).toEntity();
        snapshotMapper.insert(snapshot);
    }

    private void ensureNoRunningChange(Long resourceId) {
        ensureNoRunningChange(resourceId, null);
    }

    private void ensureNoRunningChange(Long resourceId, Long excludedChangeId) {
        LambdaQueryWrapper<AdminGovernanceChange> wrapper = new LambdaQueryWrapper<AdminGovernanceChange>()
                .eq(AdminGovernanceChange::getResourceId, resourceId)
                .in(AdminGovernanceChange::getStatus, GovernanceChangeStatus.PUBLISHING.name(),
                        GovernanceChangeStatus.ROLLBACKING.name());
        if (excludedChangeId != null) {
            wrapper.ne(AdminGovernanceChange::getId, excludedChangeId);
        }
        Long runningCount = changeMapper.selectCount(wrapper);
        if (runningCount != null && runningCount > 0) {
            throw new BizException(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
    }

    private void recordAudit(AdminGovernanceChange change, String operation, String operatorId, String result,
                             String detailSummary, String errorCode, String errorMessage) {
        auditApplicationService.record(GovernanceAudit.builder()
                .domain(DOMAIN_CHANGES)
                .resourceId(change.getResourceId())
                .changeId(change.getId())
                .operation(operation)
                .operatorId(operatorId)
                .result(result)
                .detailSummary(detailSummary)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build());
    }

    private GovernancePublishResult toPublishResult(AdminGovernanceRelease release, String effectiveHint) {
        return GovernancePublishResult.builder()
                .releaseId(release.getId())
                .beforeHash(release.getBeforeHash())
                .afterHash(release.getAfterHash())
                .status(GovernanceReleaseStatus.valueOf(release.getStatus()))
                .effectiveHint(effectiveHint)
                .build();
    }
}
