package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.GovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.request.GovernancePublishRequest;
import com.fons.cloud.admin.api.request.GovernanceRollbackRequest;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceResource;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceResourceMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceSnapshotMapper;
import com.fons.cloud.admin.infrastructure.converter.GovernanceChangeConverter;
import com.fons.cloud.common.result.R;
import com.fons.cloud.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 治理发布编排服务，负责选择目标适配器并串联草稿、校验、发布、漂移和回滚状态机。
 */
@Service
public class GovernancePublishService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ChangeApplicationService changeApplicationService;
    private final AdminGovernanceResourceMapper resourceMapper;
    private final AdminGovernanceChangeMapper changeMapper;
    private final AdminGovernanceSnapshotMapper snapshotMapper;
    private final Map<GovernanceDomain, GovernanceTargetAdapter> adapters;

    /**
     * 构建治理发布编排服务。
     *
     * @param changeApplicationService 变更状态机服务
     * @param resourceMapper           治理资源 Mapper
     * @param changeMapper             治理变更 Mapper
     * @param snapshotMapper           治理快照 Mapper
     * @param adapterList              所有治理目标适配器
     */
    public GovernancePublishService(ChangeApplicationService changeApplicationService,
                                    AdminGovernanceResourceMapper resourceMapper,
                                    AdminGovernanceChangeMapper changeMapper,
                                    AdminGovernanceSnapshotMapper snapshotMapper,
                                    List<GovernanceTargetAdapter> adapterList) {
        this.changeApplicationService = changeApplicationService;
        this.resourceMapper = resourceMapper;
        this.changeMapper = changeMapper;
        this.snapshotMapper = snapshotMapper;
        this.adapters = new EnumMap<>(GovernanceDomain.class);
        if (adapterList != null) {
            adapterList.forEach(adapter -> this.adapters.put(adapter.domain(), adapter));
        }
    }

    /**
     * 创建治理草稿。
     *
     * @param request    草稿创建请求
     * @param operatorId 操作人
     * @return 治理变更响应
     */
    public R<GovernanceChangeResponse> createDraft(GovernanceDraftCreateRequest request, String operatorId) {
        GovernanceTargetAdapter adapter = adapter(request.getDomain());
        if (adapter == null) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }

        GovernanceTargetAdapter.ResourceRef requestRef = new GovernanceTargetAdapter.ResourceRef(request.getDomain(),
                request.getResourceType(), request.getResourceKey(), request.getResourceKey());
        GovernanceTargetAdapter.CurrentConfig currentConfig = adapter.loadCurrent(requestRef);
        String baseHash = StringUtils.defaultIfBlank(request.getBaseHash(),
                currentConfig == null ? null : currentConfig.contentHash());
        String targetRef = currentConfig == null || StringUtils.isBlank(currentConfig.targetRef())
                ? request.getResourceKey() : currentConfig.targetRef();
        AdminGovernanceResource resource = findOrCreateResource(request, targetRef, baseHash);

        R<AdminGovernanceChange> created = changeApplicationService.createDraft(resource.getId(), nextNo("CHG"),
                request.getChangeType() == null ? GovernanceChangeType.UPDATE : request.getChangeType(), baseHash,
                request.getContent(), null, request.getDescription(), operatorId);
        if (!created.isSuccess()) {
            return R.failed(created.getCode(), created.getMessage());
        }
        return R.ok(GovernanceChangeConverter.CONVERTER.mapToResponse(created.getData()));
    }

    /**
     * 校验治理草稿。
     *
     * @param changeId   变更 ID
     * @param operatorId 操作人
     * @return 校验结果
     */
    public R<GovernanceValidateResult> validateDraft(Long changeId, String operatorId) {
        AdminGovernanceChange change = changeMapper.selectById(changeId);
        if (change == null) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        AdminGovernanceResource resource = resourceMapper.selectById(change.getResourceId());
        GovernanceTargetAdapter adapter = adapter(resource);
        if (adapter == null) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }

        GovernanceValidateResult validateResult = adapter.validate(new GovernanceTargetAdapter.TargetConfig(
                toResourceRef(resource), change.getContent(), change.getContentHash()));
        if (validateResult == null || !Boolean.TRUE.equals(validateResult.getPassed())) {
            changeApplicationService.validateFailed(changeId, JsonUtil.toJson(validateResult), operatorId);
        } else {
            changeApplicationService.validateSucceeded(changeId, validateResult.getNormalizedContentHash(),
                    JsonUtil.toJson(validateResult), operatorId);
        }
        return R.ok(validateResult);
    }

    /**
     * 发布已校验草稿。
     *
     * @param request    发布请求
     * @param operatorId 操作人
     * @return 发布结果
     */
    public R<GovernancePublishResult> publish(GovernancePublishRequest request, String operatorId) {
        AdminGovernanceChange change = changeMapper.selectById(request.getDraftId());
        if (change == null) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        AdminGovernanceResource resource = resourceMapper.selectById(change.getResourceId());
        GovernanceTargetAdapter adapter = adapter(resource);
        if (adapter == null) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }

        GovernanceTargetAdapter.ResourceRef resourceRef = toResourceRef(resource);
        GovernanceTargetAdapter.CurrentConfig currentConfig;
        try {
            currentConfig = adapter.loadCurrent(resourceRef);
        } catch (RuntimeException ex) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        String expectedBaseHash = StringUtils.defaultIfBlank(request.getExpectedBaseHash(), change.getBaseHash());
        String releaseNo = nextNo("REL");
        R<com.fons.cloud.admin.domain.entity.AdminGovernanceRelease> begun =
                changeApplicationService.beginPublishExecution(change.getId(), releaseNo,
                        currentConfig == null ? expectedBaseHash : currentConfig.contentHash(), operatorId);
        if (!begun.isSuccess()) {
            return R.failed(begun.getCode(), begun.getMessage());
        }
        Long releaseId = begun.getData().getId();
        if (currentConfig != null && StringUtils.isNotBlank(expectedBaseHash)
                && !expectedBaseHash.equals(currentConfig.contentHash())) {
            return changeApplicationService.driftPublishExecution(change.getId(), releaseId,
                    currentConfig.content(), currentConfig.contentHash(), operatorId);
        }

        GovernanceTargetAdapter.AdapterPublishResult publishResult;
        try {
            publishResult = adapter.publish(
                    new GovernanceTargetAdapter.TargetConfig(resourceRef, change.getContent(), change.getContentHash()),
                    new GovernanceTargetAdapter.PublishContext(releaseNo, operatorId, request.getPublishReason(),
                            expectedBaseHash));
        } catch (RuntimeException ex) {
            return changeApplicationService.failPublishExecution(change.getId(), releaseId,
                    AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), "治理目标写入明确失败", operatorId);
        }
        if (publishResult == null || !publishResult.success()) {
            return changeApplicationService.failPublishExecution(change.getId(), releaseId,
                    publishResult == null ? AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode() : publishResult.errorCode(),
                    publishResult == null ? AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getMessage() : publishResult.errorMessage(),
                    operatorId);
        }

        GovernanceTargetAdapter.CurrentConfig confirmed;
        try {
            confirmed = adapter.loadCurrent(resourceRef);
        } catch (RuntimeException ex) {
            return changeApplicationService.pendingConfirmExecution(change.getId(), releaseId,
                    publishResult.beforeContent(), publishResult.beforeHash(), publishResult.afterHash(),
                    "写入后目标回读失败", operatorId);
        }
        if (confirmed == null || StringUtils.isBlank(confirmed.contentHash())
                || !confirmed.contentHash().equals(publishResult.afterHash())) {
            return changeApplicationService.pendingConfirmExecution(change.getId(), releaseId,
                    publishResult.beforeContent(), publishResult.beforeHash(), publishResult.afterHash(),
                    "写入后目标摘要不一致", operatorId);
        }
        updateResourceHash(resource, confirmed.contentHash());
        return changeApplicationService.completePublishExecution(change.getId(), releaseId,
                publishResult.beforeContent(), publishResult.beforeHash(), confirmed.content(), confirmed.contentHash(),
                operatorId, publishResult.effectiveHint());
    }

    /**
     * 基于历史快照发起回滚并发布。
     *
     * @param request    回滚请求
     * @param operatorId 操作人
     * @return 发布结果
     */
    public R<GovernancePublishResult> rollback(GovernanceRollbackRequest request, String operatorId) {
        AdminGovernanceSnapshot sourceSnapshot = snapshotMapper.selectById(request.getSnapshotId());
        if (sourceSnapshot == null) {
            return R.failed(AdminResultCode.ADMIN_ROLLBACK_UNSUPPORTED);
        }
        AdminGovernanceResource resource = resourceMapper.selectById(sourceSnapshot.getResourceId());
        GovernanceTargetAdapter adapter = adapter(resource);
        if (adapter == null || !adapter.rollbackSupported(toResourceRef(resource))) {
            return R.failed(AdminResultCode.ADMIN_ROLLBACK_UNSUPPORTED);
        }

        R<AdminGovernanceChange> rollbackChange = changeApplicationService.createRollbackChange(request.getSnapshotId(),
                nextNo("CHG-RB"), operatorId, request.getRollbackReason());
        if (!rollbackChange.isSuccess()) {
            return R.failed(rollbackChange.getCode(), rollbackChange.getMessage());
        }

        GovernanceTargetAdapter.ResourceRef resourceRef = toResourceRef(resource);
        GovernanceTargetAdapter.CurrentConfig currentConfig;
        try {
            currentConfig = adapter.loadCurrent(resourceRef);
        } catch (RuntimeException ex) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        String expectedCurrentHash = StringUtils.defaultIfBlank(request.getExpectedCurrentHash(),
                currentConfig == null ? null : currentConfig.contentHash());
        String releaseNo = nextNo("REL-RB");
        R<com.fons.cloud.admin.domain.entity.AdminGovernanceRelease> begun =
                changeApplicationService.beginRollbackExecution(rollbackChange.getData().getId(), releaseNo,
                        currentConfig == null ? expectedCurrentHash : currentConfig.contentHash(), operatorId);
        if (!begun.isSuccess()) {
            return R.failed(begun.getCode(), begun.getMessage());
        }
        Long releaseId = begun.getData().getId();
        if (currentConfig != null && StringUtils.isNotBlank(expectedCurrentHash)
                && !expectedCurrentHash.equals(currentConfig.contentHash())) {
            return changeApplicationService.failRollbackExecution(rollbackChange.getData().getId(), releaseId,
                    AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getCode(),
                    AdminResultCode.ADMIN_CONFIG_DRIFT_DETECTED.getMessage(), operatorId);
        }

        GovernanceTargetAdapter.AdapterPublishResult publishResult;
        try {
            publishResult = adapter.publish(
                    new GovernanceTargetAdapter.TargetConfig(resourceRef, sourceSnapshot.getContent(),
                            sourceSnapshot.getContentHash()),
                    new GovernanceTargetAdapter.PublishContext(releaseNo, operatorId, request.getRollbackReason(),
                            expectedCurrentHash));
        } catch (RuntimeException ex) {
            return changeApplicationService.failRollbackExecution(rollbackChange.getData().getId(), releaseId,
                    AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), "治理目标回滚明确失败", operatorId);
        }
        if (publishResult == null || !publishResult.success()) {
            return changeApplicationService.failRollbackExecution(rollbackChange.getData().getId(), releaseId,
                    publishResult == null ? AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode() : publishResult.errorCode(),
                    publishResult == null ? AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getMessage() : publishResult.errorMessage(),
                    operatorId);
        }

        GovernanceTargetAdapter.CurrentConfig confirmed;
        try {
            confirmed = adapter.loadCurrent(resourceRef);
        } catch (RuntimeException ex) {
            return changeApplicationService.pendingRollbackConfirmExecution(rollbackChange.getData().getId(),
                    releaseId, publishResult.beforeContent(), publishResult.beforeHash(), publishResult.afterHash(),
                    "回滚写入后目标回读失败", operatorId);
        }
        if (confirmed == null || StringUtils.isBlank(confirmed.contentHash())
                || !confirmed.contentHash().equals(publishResult.afterHash())) {
            return changeApplicationService.pendingRollbackConfirmExecution(rollbackChange.getData().getId(),
                    releaseId, publishResult.beforeContent(), publishResult.beforeHash(), publishResult.afterHash(),
                    "回滚写入后目标摘要不一致", operatorId);
        }
        updateResourceHash(resource, confirmed.contentHash());
        return changeApplicationService.completeRollbackExecution(rollbackChange.getData().getId(), releaseId,
                publishResult.beforeContent(), publishResult.beforeHash(), confirmed.content(), confirmed.contentHash(),
                operatorId, publishResult.effectiveHint());
    }

    private GovernanceTargetAdapter adapter(GovernanceDomain domain) {
        return domain == null ? null : adapters.get(domain);
    }

    private GovernanceTargetAdapter adapter(AdminGovernanceResource resource) {
        if (resource == null) {
            return null;
        }
        return adapter(GovernanceDomain.valueOf(resource.getDomain().toUpperCase()));
    }

    private AdminGovernanceResource findOrCreateResource(GovernanceDraftCreateRequest request, String targetRef,
                                                         String currentHash) {
        AdminGovernanceResource resource = resourceMapper.selectOne(new LambdaQueryWrapper<AdminGovernanceResource>()
                .eq(AdminGovernanceResource::getDomain, request.getDomain().getCode())
                .eq(AdminGovernanceResource::getResourceType, request.getResourceType())
                .eq(AdminGovernanceResource::getResourceKey, request.getResourceKey()));
        if (resource != null) {
            return resource;
        }
        resource = new AdminGovernanceResource();
        resource.setDomain(request.getDomain().getCode());
        resource.setResourceType(request.getResourceType());
        resource.setResourceKey(request.getResourceKey());
        resource.setTargetRef(targetRef);
        resource.setCurrentHash(currentHash);
        resource.setStatus("ACTIVE");
        resource.setDescription(request.getDescription());
        resourceMapper.insert(resource);
        return resource;
    }

    private GovernanceTargetAdapter.ResourceRef toResourceRef(AdminGovernanceResource resource) {
        return new GovernanceTargetAdapter.ResourceRef(GovernanceDomain.valueOf(resource.getDomain().toUpperCase()),
                resource.getResourceType(), resource.getResourceKey(), resource.getTargetRef());
    }

    private void updateResourceHash(AdminGovernanceResource resource, String currentHash) {
        if (resource != null && StringUtils.isNotBlank(currentHash)) {
            resource.setCurrentHash(currentHash);
            resourceMapper.updateById(resource);
        }
    }

    private String nextNo(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(NO_FORMATTER) + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }
}
