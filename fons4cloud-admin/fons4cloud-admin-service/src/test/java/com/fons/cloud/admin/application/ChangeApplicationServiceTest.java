package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.enums.GovernanceAuditResult;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.api.enums.GovernanceSnapshotType;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceReleaseMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceSnapshotMapper;
import com.fons.cloud.admin.domain.model.GovernanceAudit;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 统一治理变更应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ChangeApplicationServiceTest {

    @Mock
    private AdminGovernanceChangeMapper changeMapper;
    @Mock
    private AdminGovernanceReleaseMapper releaseMapper;
    @Mock
    private AdminGovernanceSnapshotMapper snapshotMapper;
    @Mock
    private AuditApplicationService auditApplicationService;

    private ChangeApplicationService changeApplicationService;

    @BeforeEach
    void setUp() {
        changeApplicationService = new ChangeApplicationService(changeMapper, releaseMapper, snapshotMapper,
                auditApplicationService);
        lenient().when(changeMapper.updateById(any(AdminGovernanceChange.class))).thenReturn(1);
        lenient().when(changeMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        lenient().when(releaseMapper.updateById(any(AdminGovernanceRelease.class))).thenReturn(1);
        lenient().when(snapshotMapper.insert(any(AdminGovernanceSnapshot.class))).thenReturn(1);
        lenient().when(auditApplicationService.record(any(GovernanceAudit.class))).thenReturn(R.ok(Boolean.TRUE));
        AtomicLong releaseId = new AtomicLong(500L);
        lenient().when(releaseMapper.insert(any(AdminGovernanceRelease.class))).thenAnswer(invocation -> {
            AdminGovernanceRelease release = invocation.getArgument(0);
            release.setId(releaseId.incrementAndGet());
            return 1;
        });
    }

    @Test
    void createDraftShouldPersistDraftAndRecordAudit() {
        when(changeMapper.insert(any(AdminGovernanceChange.class))).thenAnswer(invocation -> {
            AdminGovernanceChange change = invocation.getArgument(0);
            change.setId(9L);
            return 1;
        });

        R<AdminGovernanceChange> response = changeApplicationService.createDraft(100L, "CHG-NEW",
                GovernanceChangeType.UPDATE, "hash-base", "{\"route\":\"demo\"}", "hash-content",
                "新增路由", "creator");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getId()).isEqualTo(9L);
        assertThat(response.getData().getStatus()).isEqualTo(GovernanceChangeStatus.DRAFT.name());
        assertThat(response.getData().getContentHash()).isEqualTo("hash-content");
    }

    @Test
    void validateSuccessAndFailureShouldUpdateChangeAndAuditResult() {
        AdminGovernanceChange successDraft = change(10L, GovernanceChangeStatus.DRAFT, GovernanceChangeType.UPDATE);
        AdminGovernanceChange failedDraft = change(11L, GovernanceChangeStatus.DRAFT, GovernanceChangeType.UPDATE);
        when(changeMapper.selectById(10L)).thenReturn(successDraft);
        when(changeMapper.selectById(11L)).thenReturn(failedDraft);

        R<Boolean> success = changeApplicationService.validateSucceeded(10L, "hash-validated",
                "{\"passed\":true}", "validator");
        R<Boolean> failed = changeApplicationService.validateFailed(11L, "{\"errors\":[\"routeId缺失\"]}",
                "validator");

        assertThat(success.isSuccess()).isTrue();
        assertThat(successDraft.getStatus()).isEqualTo(GovernanceChangeStatus.VALIDATED.name());
        assertThat(successDraft.getContentHash()).isEqualTo("hash-validated");
        assertThat(failed.isSuccess()).isTrue();
        assertThat(failedDraft.getStatus()).isEqualTo(GovernanceChangeStatus.VALIDATION_FAILED.name());

        ArgumentCaptor<GovernanceAudit> auditCaptor = ArgumentCaptor.forClass(GovernanceAudit.class);
        org.mockito.Mockito.verify(auditApplicationService, org.mockito.Mockito.times(2)).record(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues())
                .extracting(GovernanceAudit::getResult)
                .containsExactly(GovernanceAuditResult.SUCCESS.name(), GovernanceAuditResult.FAILED.name());
    }

    @Test
    void publishSuccessFailureAndDriftShouldCreateReleaseSnapshotsAndAudit() {
        AdminGovernanceChange successChange = change(20L, GovernanceChangeStatus.VALIDATED, GovernanceChangeType.UPDATE);
        AdminGovernanceChange failedChange = change(21L, GovernanceChangeStatus.VALIDATED, GovernanceChangeType.UPDATE);
        AdminGovernanceChange driftChange = change(22L, GovernanceChangeStatus.VALIDATED, GovernanceChangeType.UPDATE);
        when(changeMapper.selectById(20L)).thenReturn(successChange);
        when(changeMapper.selectById(21L)).thenReturn(failedChange);
        when(changeMapper.selectById(22L)).thenReturn(driftChange);

        R<GovernancePublishResult> publish = changeApplicationService.publishSucceeded(20L, "REL-001",
                "{\"old\":true}", "hash-old", "{\"new\":true}", "hash-new", "publisher", "Nacos监听刷新");
        R<GovernancePublishResult> failed = changeApplicationService.publishFailed(21L, "REL-002", "hash-old",
                "TARGET_ERROR", "clientSecret=should-mask", "publisher");
        R<GovernancePublishResult> drift = changeApplicationService.detectDrift(22L, "REL-003",
                "{\"ops\":true}", "hash-ops", "publisher");

        assertThat(publish.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.SUCCESS);
        assertThat(successChange.getStatus()).isEqualTo(GovernanceChangeStatus.PUBLISHED.name());
        assertThat(failed.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.FAILED);
        assertThat(failedChange.getStatus()).isEqualTo(GovernanceChangeStatus.PUBLISH_FAILED.name());
        assertThat(drift.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.DRIFT_DETECTED);
        assertThat(driftChange.getStatus()).isEqualTo(GovernanceChangeStatus.DRIFT_DETECTED.name());

        ArgumentCaptor<AdminGovernanceSnapshot> snapshotCaptor = ArgumentCaptor.forClass(AdminGovernanceSnapshot.class);
        org.mockito.Mockito.verify(snapshotMapper, atLeast(3)).insert(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getAllValues())
                .extracting(AdminGovernanceSnapshot::getSnapshotType)
                .contains(GovernanceSnapshotType.BEFORE.name(), GovernanceSnapshotType.AFTER.name(),
                        GovernanceSnapshotType.DRIFT_CURRENT.name());
    }

    @Test
    void rollbackShouldCreateNewChangeAndKeepSourceSnapshotHistory() {
        AdminGovernanceSnapshot source = new AdminGovernanceSnapshot();
        source.setId(900L);
        source.setResourceId(100L);
        source.setChangeId(20L);
        source.setReleaseId(501L);
        source.setSnapshotType(GovernanceSnapshotType.AFTER.name());
        source.setContent("{\"new\":true}");
        source.setContentHash("hash-new");
        when(snapshotMapper.selectById(900L)).thenReturn(source);
        when(changeMapper.insert(any(AdminGovernanceChange.class))).thenAnswer(invocation -> {
            AdminGovernanceChange change = invocation.getArgument(0);
            change.setId(30L);
            return 1;
        });

        R<AdminGovernanceChange> rollbackDraft = changeApplicationService.createRollbackChange(900L, "CHG-RB-001",
                "rollbacker", "回滚到上一版本");

        assertThat(rollbackDraft.isSuccess()).isTrue();
        assertThat(rollbackDraft.getData().getId()).isEqualTo(30L);
        assertThat(rollbackDraft.getData().getChangeType()).isEqualTo(GovernanceChangeType.ROLLBACK.name());
        assertThat(rollbackDraft.getData().getStatus()).isEqualTo(GovernanceChangeStatus.VALIDATED.name());
        assertThat(source.getSnapshotType()).isEqualTo(GovernanceSnapshotType.AFTER.name());

        AdminGovernanceChange rollbackChange = rollbackDraft.getData();
        AdminGovernanceChange rollbackFailedChange = change(31L, GovernanceChangeStatus.VALIDATED,
                GovernanceChangeType.ROLLBACK);
        when(changeMapper.selectById(30L)).thenReturn(rollbackChange);
        when(changeMapper.selectById(31L)).thenReturn(rollbackFailedChange);

        R<GovernancePublishResult> rollbackSuccess = changeApplicationService.rollbackSucceeded(30L, "REL-RB-001",
                "{\"current\":true}", "hash-current", "{\"new\":true}", "hash-new", "rollbacker", "回滚发布成功");
        R<GovernancePublishResult> rollbackFailed = changeApplicationService.rollbackFailed(31L, "REL-RB-002",
                "hash-current", "ROLLBACK_ERROR", "target unavailable", "rollbacker");

        assertThat(rollbackSuccess.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.SUCCESS);
        assertThat(rollbackChange.getStatus()).isEqualTo(GovernanceChangeStatus.ROLLED_BACK.name());
        assertThat(rollbackFailed.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.FAILED);
        assertThat(rollbackFailedChange.getStatus()).isEqualTo(GovernanceChangeStatus.ROLLBACK_FAILED.name());
    }

    private AdminGovernanceChange change(Long id, GovernanceChangeStatus status, GovernanceChangeType type) {
        AdminGovernanceChange change = new AdminGovernanceChange();
        change.setId(id);
        change.setResourceId(100L);
        change.setChangeNo("CHG-" + id);
        change.setChangeType(type.name());
        change.setStatus(status.name());
        change.setBaseHash("hash-base");
        change.setContent("{\"route\":\"demo\"}");
        change.setContentHash("hash-content");
        change.setCreatedBy("creator");
        change.setUpdatedBy("creator");
        return change;
    }
}
