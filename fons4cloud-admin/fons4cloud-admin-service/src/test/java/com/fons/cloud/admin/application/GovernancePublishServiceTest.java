package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.api.request.GovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.request.GovernancePublishRequest;
import com.fons.cloud.admin.api.request.GovernanceRollbackRequest;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceResource;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceResourceMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceSnapshotMapper;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 治理发布编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class GovernancePublishServiceTest {

    @Mock
    private ChangeApplicationService changeApplicationService;
    @Mock
    private AdminGovernanceResourceMapper resourceMapper;
    @Mock
    private AdminGovernanceChangeMapper changeMapper;
    @Mock
    private AdminGovernanceSnapshotMapper snapshotMapper;
    @Mock
    private GovernanceTargetAdapter adapter;

    private GovernancePublishService governancePublishService;

    @BeforeEach
    void setUp() {
        when(adapter.domain()).thenReturn(GovernanceDomain.GATEWAY);
        governancePublishService = new GovernancePublishService(changeApplicationService, resourceMapper, changeMapper,
                snapshotMapper, List.of(adapter));
    }

    @Test
    void createDraftShouldLoadCurrentConfigAndCreateResource() {
        GovernanceDraftCreateRequest request = draftRequest();
        when(adapter.loadCurrent(any())).thenReturn(new GovernanceTargetAdapter.CurrentConfig("{\"old\":true}",
                "hash-current", "gateway-routing.json"));
        when(resourceMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(resourceMapper.insert(any(AdminGovernanceResource.class))).thenAnswer(invocation -> {
            AdminGovernanceResource resource = invocation.getArgument(0);
            resource.setId(100L);
            return 1;
        });
        AdminGovernanceChange change = change(10L, 100L, GovernanceChangeStatus.DRAFT);
        when(changeApplicationService.createDraft(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(R.ok(change));

        R<?> response = governancePublishService.createDraft(request, "operator");

        assertThat(response.isSuccess()).isTrue();
        verify(adapter).loadCurrent(any());
        verify(resourceMapper).insert(any(AdminGovernanceResource.class));
    }

    @Test
    void validateDraftShouldMarkSuccessWhenAdapterPassed() {
        AdminGovernanceChange change = change(10L, 100L, GovernanceChangeStatus.DRAFT);
        when(changeMapper.selectById(10L)).thenReturn(change);
        when(resourceMapper.selectById(100L)).thenReturn(resource());
        when(adapter.validate(any())).thenReturn(GovernanceValidateResult.builder()
                .passed(Boolean.TRUE)
                .normalizedContentHash("hash-validated")
                .build());
        when(changeApplicationService.validateSucceeded(any(), any(), any(), any()))
                .thenReturn(R.ok(Boolean.TRUE));

        R<GovernanceValidateResult> response = governancePublishService.validateDraft(10L, "operator");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getPassed()).isTrue();
        verify(changeApplicationService).validateSucceeded(any(), any(), any(), any());
    }

    @Test
    void publishShouldCallAdapterAndMarkSuccessWhenHashNotDrifted() {
        AdminGovernanceChange change = change(10L, 100L, GovernanceChangeStatus.VALIDATED);
        when(changeMapper.selectById(10L)).thenReturn(change);
        when(resourceMapper.selectById(100L)).thenReturn(resource());
        when(adapter.loadCurrent(any())).thenReturn(new GovernanceTargetAdapter.CurrentConfig("{\"old\":true}",
                "hash-base", "gateway-routing.json"));
        when(adapter.publish(any(), any())).thenReturn(new GovernanceTargetAdapter.AdapterPublishResult(true,
                "{\"old\":true}", "hash-base", "{\"new\":true}", "hash-new", null, null, "Nacos监听刷新"));
        when(changeApplicationService.publishSucceeded(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(R.ok(GovernancePublishResult.builder().status(GovernanceReleaseStatus.SUCCESS).build()));

        R<GovernancePublishResult> response = governancePublishService.publish(GovernancePublishRequest.builder()
                .draftId(10L)
                .expectedBaseHash("hash-base")
                .publishReason("publish")
                .build(), "operator");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.SUCCESS);
        verify(adapter).publish(any(), any());
        verify(resourceMapper).updateById(any(AdminGovernanceResource.class));
    }

    @Test
    void publishShouldMarkDriftWhenCurrentHashChanged() {
        AdminGovernanceChange change = change(10L, 100L, GovernanceChangeStatus.VALIDATED);
        when(changeMapper.selectById(10L)).thenReturn(change);
        when(resourceMapper.selectById(100L)).thenReturn(resource());
        when(adapter.loadCurrent(any())).thenReturn(new GovernanceTargetAdapter.CurrentConfig("{\"ops\":true}",
                "hash-ops", "gateway-routing.json"));
        when(changeApplicationService.detectDrift(any(), any(), any(), any(), any()))
                .thenReturn(R.ok(GovernancePublishResult.builder()
                        .status(GovernanceReleaseStatus.DRIFT_DETECTED)
                        .build()));

        R<GovernancePublishResult> response = governancePublishService.publish(GovernancePublishRequest.builder()
                .draftId(10L)
                .expectedBaseHash("hash-base")
                .build(), "operator");

        assertThat(response.getData().getStatus()).isEqualTo(GovernanceReleaseStatus.DRIFT_DETECTED);
        verify(changeApplicationService).detectDrift(any(), any(), any(), any(), any());
    }

    @Test
    void rollbackShouldRejectUnsupportedTarget() {
        AdminGovernanceSnapshot snapshot = new AdminGovernanceSnapshot();
        snapshot.setId(900L);
        snapshot.setResourceId(100L);
        when(snapshotMapper.selectById(900L)).thenReturn(snapshot);
        when(resourceMapper.selectById(100L)).thenReturn(resource());
        when(adapter.rollbackSupported(any())).thenReturn(false);

        R<GovernancePublishResult> response = governancePublishService.rollback(GovernanceRollbackRequest.builder()
                .snapshotId(900L)
                .expectedCurrentHash("hash-base")
                .rollbackReason("rollback")
                .build(), "operator");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_ROLLBACK_UNSUPPORTED.getCode());
    }

    private GovernanceDraftCreateRequest draftRequest() {
        return GovernanceDraftCreateRequest.builder()
                .domain(GovernanceDomain.GATEWAY)
                .resourceType("ROUTE")
                .resourceKey("gateway-routing.json")
                .content("{\"new\":true}")
                .changeType(GovernanceChangeType.UPDATE)
                .description("publish route")
                .build();
    }

    private AdminGovernanceResource resource() {
        AdminGovernanceResource resource = new AdminGovernanceResource();
        resource.setId(100L);
        resource.setDomain(GovernanceDomain.GATEWAY.getCode());
        resource.setResourceType("ROUTE");
        resource.setResourceKey("gateway-routing.json");
        resource.setTargetRef("gateway-routing.json");
        resource.setCurrentHash("hash-base");
        resource.setStatus("ACTIVE");
        return resource;
    }

    private AdminGovernanceChange change(Long id, Long resourceId, GovernanceChangeStatus status) {
        AdminGovernanceChange change = new AdminGovernanceChange();
        change.setId(id);
        change.setResourceId(resourceId);
        change.setChangeNo("CHG-" + id);
        change.setChangeType(GovernanceChangeType.UPDATE.name());
        change.setStatus(status.name());
        change.setBaseHash("hash-base");
        change.setContent("{\"new\":true}");
        change.setContentHash("hash-new");
        return change;
    }
}
