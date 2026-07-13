package com.fons.cloud.admin.application;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.api.enums.GovernanceReleaseType;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.entity.AdminGovernanceResource;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceReleaseMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceResourceMapper;
import com.fons.cloud.common.result.R;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 对 PENDING_CONFIRM 发布只做目标回读，不自动重复写入外部目标。
 */
@Service
public class GovernanceExecutionRecoveryService {

    private final ChangeApplicationService changeApplicationService;
    private final AdminGovernanceReleaseMapper releaseMapper;
    private final AdminGovernanceChangeMapper changeMapper;
    private final AdminGovernanceResourceMapper resourceMapper;
    private final Map<GovernanceDomain, GovernanceTargetAdapter> adapters = new EnumMap<>(GovernanceDomain.class);

    public GovernanceExecutionRecoveryService(ChangeApplicationService changeApplicationService,
                                              AdminGovernanceReleaseMapper releaseMapper,
                                              AdminGovernanceChangeMapper changeMapper,
                                              AdminGovernanceResourceMapper resourceMapper,
                                              List<GovernanceTargetAdapter> adapterList) {
        this.changeApplicationService = changeApplicationService;
        this.releaseMapper = releaseMapper;
        this.changeMapper = changeMapper;
        this.resourceMapper = resourceMapper;
        adapterList.forEach(adapter -> adapters.put(adapter.domain(), adapter));
    }

    public R<GovernancePublishResult> recover(Long releaseId, String operatorId) {
        AdminGovernanceRelease release = releaseMapper.selectById(releaseId);
        if (release == null || !GovernanceReleaseStatus.PENDING_CONFIRM.name().equals(release.getStatus())) {
            return R.failed(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE);
        }
        AdminGovernanceChange change = changeMapper.selectById(release.getChangeId());
        AdminGovernanceResource resource = change == null ? null : resourceMapper.selectById(change.getResourceId());
        if (resource == null) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        GovernanceDomain domain = GovernanceDomain.valueOf(resource.getDomain().toUpperCase());
        GovernanceTargetAdapter adapter = adapters.get(domain);
        if (adapter == null) {
            return R.failed(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        GovernanceTargetAdapter.CurrentConfig current;
        try {
            current = adapter.loadCurrent(new GovernanceTargetAdapter.ResourceRef(domain, resource.getResourceType(),
                    resource.getResourceKey(), resource.getTargetRef()));
        } catch (RuntimeException ex) {
            return R.ok(pending(release));
        }
        if (current != null && current.contentHash() != null && current.contentHash().equals(release.getAfterHash())) {
            return GovernanceReleaseType.ROLLBACK.name().equals(release.getReleaseType())
                    ? changeApplicationService.confirmRecoveredRollbackSuccess(releaseId, current.content(),
                    current.contentHash(), operatorId)
                    : changeApplicationService.confirmRecoveredSuccess(releaseId, current.content(),
                    current.contentHash(), operatorId);
        }
        if (current != null && current.contentHash() != null && current.contentHash().equals(release.getBeforeHash())) {
            return GovernanceReleaseType.ROLLBACK.name().equals(release.getReleaseType())
                    ? changeApplicationService.confirmRecoveredRollbackNotApplied(releaseId, operatorId)
                    : changeApplicationService.confirmRecoveredNotApplied(releaseId, operatorId);
        }
        return R.ok(pending(release));
    }

    private GovernancePublishResult pending(AdminGovernanceRelease release) {
        return GovernancePublishResult.builder()
                .releaseId(release.getId())
                .beforeHash(release.getBeforeHash())
                .afterHash(release.getAfterHash())
                .status(GovernanceReleaseStatus.PENDING_CONFIRM)
                .effectiveHint("目标当前摘要既不等于执行前也不等于期望值，需要人工核查")
                .build();
    }
}
