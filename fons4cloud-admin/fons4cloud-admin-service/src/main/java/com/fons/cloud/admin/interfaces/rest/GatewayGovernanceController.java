package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.GroupedGovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.application.GovernancePublishService;
import com.fons.cloud.admin.infrastructure.converter.GovernanceDraftRequestConverter;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关治理 REST API。
 */
@RestController
@RequestMapping("/admin/gateway/routes")
@RequiredArgsConstructor
public class GatewayGovernanceController {

    private static final String RESOURCE_TYPE_ROUTE = "ROUTE";

    private final GovernancePublishService governancePublishService;

    /**
     * 创建网关路由治理草稿。
     *
     * @param request 草稿创建请求
     * @return 治理变更响应
    */
    @PostMapping("/drafts")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.GATEWAY_EDIT)
    public R<GovernanceChangeResponse> createRouteDraft(
            @Valid @RequestBody GroupedGovernanceDraftCreateRequest request) {
        return governancePublishService.createDraft(GovernanceDraftRequestConverter.CONVERTER.mapToCreateRequest(
                request, GovernanceDomain.GATEWAY, RESOURCE_TYPE_ROUTE), currentOperatorId());
    }

    private String currentOperatorId() {
        return String.valueOf(AuthUtils.getCurrentUser().getId());
    }
}
