package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.enums.GovernanceAuditResult;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.AuditQueryRequest;
import com.fons.cloud.admin.domain.entity.AdminGovernanceAudit;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceAuditMapper;
import com.fons.cloud.admin.domain.model.GovernanceAudit;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * admin 审计应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AuditApplicationServiceTest {

    @Mock
    private AdminGovernanceAuditMapper adminGovernanceAuditMapper;

    private AuditApplicationService auditApplicationService;

    @BeforeEach
    void setUp() {
        auditApplicationService = new AuditApplicationService(adminGovernanceAuditMapper);
    }

    @Test
    void recordShouldMaskSensitiveSummaryBeforeInsert() {
        ArgumentCaptor<AdminGovernanceAudit> captor = ArgumentCaptor.forClass(AdminGovernanceAudit.class);
        when(adminGovernanceAuditMapper.insert(captor.capture())).thenReturn(1);

        R<Boolean> result = auditApplicationService.record(GovernanceAudit.builder()
                .domain("access")
                .operation("ROLE_GRANT")
                .operatorId("root")
                .result(GovernanceAuditResult.SUCCESS.name())
                .detailSummary("clientSecret=abc token=access-token routeCount=2")
                .errorMessage("refreshToken=refresh-token")
                .build());

        assertThat(result.isSuccess()).isTrue();
        assertThat(captor.getValue().getDetailSummary()).doesNotContain("abc", "access-token");
        assertThat(captor.getValue().getErrorMessage()).doesNotContain("refresh-token");
    }

    @Test
    void queryShouldMapAuditList() {
        AdminGovernanceAudit audit = auditEntity();
        when(adminGovernanceAuditMapper.selectList(any(Wrapper.class))).thenReturn(List.of(audit));

        R<?> result = auditApplicationService.query(AuditQueryRequest.builder()
                .domain(GovernanceDomain.ACCESS)
                .operation("ROLE_GRANT")
                .operatorId("root")
                .result(GovernanceAuditResult.SUCCESS)
                .startTime(new Date(1L))
                .endTime(new Date())
                .resourceKey("10")
                .build());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).asList().hasSize(1);
    }

    @Test
    void getByIdShouldReturnAuditDetail() {
        when(adminGovernanceAuditMapper.selectById(1L)).thenReturn(auditEntity());

        R<?> result = auditApplicationService.getById(1L);

        assertThat(result.isSuccess()).isTrue();
    }

    private AdminGovernanceAudit auditEntity() {
        AdminGovernanceAudit audit = new AdminGovernanceAudit();
        audit.setId(1L);
        audit.setDomain("access");
        audit.setResourceId(10L);
        audit.setOperation("ROLE_GRANT");
        audit.setOperatorId("root");
        audit.setResult(GovernanceAuditResult.SUCCESS.name());
        audit.setDetailSummary("role grant");
        audit.setOperatedAt(new Date());
        return audit;
    }
}
