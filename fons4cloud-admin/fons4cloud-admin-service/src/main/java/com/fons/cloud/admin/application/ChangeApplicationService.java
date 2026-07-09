package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return R.ok(changeMapper.selectList(wrapper).stream()
                .map(GovernanceChangeConverter.CONVERTER::mapToResponse)
                .toList());
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
        Long runningCount = changeMapper.selectCount(new LambdaQueryWrapper<AdminGovernanceChange>()
                .eq(AdminGovernanceChange::getResourceId, resourceId)
                .in(AdminGovernanceChange::getStatus, GovernanceChangeStatus.PUBLISHING.name(),
                        GovernanceChangeStatus.ROLLBACKING.name()));
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
