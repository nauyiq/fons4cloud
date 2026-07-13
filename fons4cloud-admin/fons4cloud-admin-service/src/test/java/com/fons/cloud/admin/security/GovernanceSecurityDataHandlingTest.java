package com.fons.cloud.admin.security;

import com.fons.cloud.admin.api.enums.GovernanceReleaseType;
import com.fons.cloud.admin.api.enums.GovernanceSnapshotType;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.model.GovernanceAudit;
import com.fons.cloud.admin.domain.model.GovernanceRelease;
import com.fons.cloud.admin.domain.model.GovernanceSnapshot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;

/**
 * 治理数据安全处理测试，集中验证审计、发布和快照摘要不会暴露敏感正文。
 */
class GovernanceSecurityDataHandlingTest {

    @Test
    void auditMaskShouldHideTokenAndSecretValues() {
        String masked = GovernanceAudit.mask("""
                clientSecret=admin-secret accessToken=access-token refreshToken=refresh-token password=pwd token=raw-token
                """);

        assertThat(masked)
                .doesNotContain("admin-secret", "access-token", "refresh-token", "pwd", "raw-token")
                .contains("clientSecret=***", "accessToken=***", "refreshToken=***", "password=***", "token=***");
    }

    @Test
    void releaseFailureShouldMaskSecretValues() {
        GovernanceRelease release = GovernanceRelease.createRunning(10L, "REL-001", GovernanceReleaseType.PUBLISH,
                "hash-before", "operator");

        release.markFailed("TARGET_ERROR", "target failed clientSecret=plain-secret token=raw-token");

        AdminGovernanceRelease entity = release.getEntity();
        assertThat(entity.getErrorMessage()).doesNotContain("plain-secret", "raw-token");
        assertThat(entity.getErrorMessage()).contains("clientSecret=***", "token=***");
    }

    @Test
    void snapshotSummaryShouldNotExposeConfigBodyIpListOrSecrets() {
        GovernanceSnapshot snapshot = GovernanceSnapshot.create(100L, 10L, 20L, GovernanceSnapshotType.AFTER,
                """
                        {"whiteIps":["10.0.0.1"],"routes":[{"id":"admin-route","uri":"lb://admin-service"}],"clientSecret":"plain-secret","token":"raw-token"}
                        """, null);

        AdminGovernanceSnapshot entity = snapshot.toEntity();

        assertThat(entity.getContent()).contains("10.0.0.1", "admin-route", "plain-secret", "raw-token");
        assertThat(entity.getContentSummary())
                .doesNotContain("10.0.0.1", "admin-route", "lb://admin-service", "plain-secret", "raw-token")
                .contains("配置内容已脱敏");
    }

    @Test
    void pendingConfirmShouldKeepOnlyExpectedHashesAndMaskedError() {
        GovernanceRelease release = GovernanceRelease.createRunning(10L, "REL-002", GovernanceReleaseType.PUBLISH,
                "hash-before", "operator");

        release.markPendingConfirm("readback failed token=raw-token", "hash-expected");

        assertThat(release.getEntity().getStatus()).isEqualTo(GovernanceReleaseStatus.PENDING_CONFIRM.name());
        assertThat(release.getEntity().getAfterHash()).isEqualTo("hash-expected");
        assertThat(release.getEntity().getErrorMessage()).doesNotContain("raw-token");
    }
}
