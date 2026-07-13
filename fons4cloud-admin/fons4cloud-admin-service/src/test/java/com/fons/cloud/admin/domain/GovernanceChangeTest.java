package com.fons.cloud.admin.domain;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.model.GovernanceChange;
import com.fons.cloud.common.base.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 治理变更状态机测试。
 */
class GovernanceChangeTest {

    @Test
    void changeShouldMoveThroughValidatePublishAndRollbackStates() {
        AdminGovernanceChange entity = change(GovernanceChangeStatus.DRAFT, GovernanceChangeType.UPDATE);
        GovernanceChange change = GovernanceChange.from(entity);

        change.startValidation("validator");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.VALIDATING.name());

        change.markValidated("hash-after-validate", "{\"passed\":true}", "validator");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.VALIDATED.name());
        assertThat(entity.getContentHash()).isEqualTo("hash-after-validate");

        change.startPublish("publisher");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.PUBLISHING.name());

        change.markPublished("publisher");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.PUBLISHED.name());

        change.startRollback("rollbacker");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.ROLLBACKING.name());

        change.markRolledBack("rollbacker");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.ROLLED_BACK.name());
    }

    @Test
    void draftShouldRejectPublishBeforeValidation() {
        GovernanceChange change = GovernanceChange.from(change(GovernanceChangeStatus.DRAFT, GovernanceChangeType.UPDATE));

        assertThatThrownBy(() -> change.startPublish("publisher"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AdminResultCode.ADMIN_DRAFT_NOT_EDITABLE.getCode());
    }

    @Test
    void publishingChangeShouldSupportFailureAndDriftStates() {
        AdminGovernanceChange entity = change(GovernanceChangeStatus.PUBLISHING, GovernanceChangeType.UPDATE);
        GovernanceChange change = GovernanceChange.from(entity);

        change.markPublishFailed("publisher");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.PUBLISH_FAILED.name());

        entity.setStatus(GovernanceChangeStatus.PUBLISHING.name());
        change.markDriftDetected("publisher");
        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.DRIFT_DETECTED.name());
    }

    @Test
    void validationFailedDraftShouldBeEditableAndReturnToDraft() {
        AdminGovernanceChange entity = change(GovernanceChangeStatus.VALIDATION_FAILED, GovernanceChangeType.UPDATE);
        entity.setValidationResult("contains-secret=false");
        GovernanceChange change = GovernanceChange.from(entity);

        change.updateDraft("{\"route\":\"updated\"}", "fix validation", "editor");

        assertThat(entity.getStatus()).isEqualTo(GovernanceChangeStatus.DRAFT.name());
        assertThat(entity.getValidationResult()).isNull();
        assertThat(entity.getContentHash()).hasSize(64);
        assertThat(entity.getUpdatedBy()).isEqualTo("editor");
    }

    private AdminGovernanceChange change(GovernanceChangeStatus status, GovernanceChangeType type) {
        AdminGovernanceChange change = new AdminGovernanceChange();
        change.setId(10L);
        change.setResourceId(100L);
        change.setChangeNo("CHG-001");
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
