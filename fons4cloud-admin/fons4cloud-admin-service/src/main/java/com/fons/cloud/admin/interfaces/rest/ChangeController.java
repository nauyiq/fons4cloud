package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.request.GovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.request.GovernancePublishRequest;
import com.fons.cloud.admin.api.request.GovernanceRollbackRequest;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.api.response.GovernancePublishResult;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.application.ChangeApplicationService;
import com.fons.cloud.admin.application.GovernancePublishService;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 治理变更 REST API，提供通用草稿、校验、发布、回滚和查询入口。
 */
@RestController
@RequestMapping("/admin/changes")
@RequiredArgsConstructor
public class ChangeController {

    private final ChangeApplicationService changeApplicationService;
    private final GovernancePublishService governancePublishService;

    /**
     * 查询治理变更列表。
     *
     * @param resourceId 治理资源 ID，可为空
     * @param status     变更状态，可为空
     * @return 治理变更列表
    */
    @GetMapping
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_VIEW)
    public R<List<GovernanceChangeResponse>> query(@RequestParam(required = false) Long resourceId,
                                                   @RequestParam(required = false) String status) {
        return changeApplicationService.query(resourceId, status);
    }

    /**
     * 查询治理变更详情。
     *
     * @param id 治理变更 ID
     * @return 治理变更详情
    */
    @GetMapping("/{id}")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_VIEW)
    public R<GovernanceChangeResponse> getById(@PathVariable Long id) {
        return changeApplicationService.getById(id);
    }

    /**
     * 创建治理草稿。
     *
     * @param request 草稿创建请求
     * @return 治理变更响应
    */
    @PostMapping("/drafts")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_EDIT)
    public R<GovernanceChangeResponse> createDraft(@Valid @RequestBody GovernanceDraftCreateRequest request) {
        return governancePublishService.createDraft(request, currentOperatorId());
    }

    /**
     * 校验治理草稿。
     *
     * @param id 治理变更 ID
     * @return 校验结果
    */
    @PostMapping("/{id}/validate")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_EDIT)
    public R<GovernanceValidateResult> validate(@PathVariable Long id) {
        return governancePublishService.validateDraft(id, currentOperatorId());
    }

    /**
     * 发布治理草稿。
     *
     * @param id      治理变更 ID
     * @param request 发布请求
     * @return 发布结果
    */
    @PostMapping("/{id}/publish")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_PUBLISH)
    public R<GovernancePublishResult> publish(@PathVariable Long id,
                                              @RequestBody(required = false) GovernancePublishRequest request) {
        GovernancePublishRequest publishRequest = request == null ? new GovernancePublishRequest() : request;
        publishRequest.setDraftId(id);
        return governancePublishService.publish(publishRequest, currentOperatorId());
    }

    /**
     * 基于历史快照发起回滚。
     *
     * @param id      当前变更 ID，用于路径语义表达，实际回滚来源以 snapshotId 为准
     * @param request 回滚请求
     * @return 发布结果
    */
    @PostMapping("/{id}/rollback")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_ROLLBACK)
    public R<GovernancePublishResult> rollback(@PathVariable Long id,
                                               @Valid @RequestBody GovernanceRollbackRequest request) {
        return governancePublishService.rollback(request, currentOperatorId());
    }

    private String currentOperatorId() {
        return String.valueOf(AuthUtils.getCurrentUser().getId());
    }
}
