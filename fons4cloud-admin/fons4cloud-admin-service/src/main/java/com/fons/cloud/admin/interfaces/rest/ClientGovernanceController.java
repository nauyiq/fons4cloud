package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.GroupedGovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.application.GovernancePublishService;
import com.fons.cloud.admin.application.GovernanceResourceQueryService;
import com.fons.cloud.admin.interfaces.rest.api.model.GovernanceResourceDetailResponse;
import com.fons.cloud.admin.interfaces.rest.api.model.PageResponse;
import com.fons.cloud.admin.infrastructure.converter.GovernanceDraftRequestConverter;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证客户端治理 REST API。
 */
@RestController
@RequestMapping("/admin/clients")
@RequiredArgsConstructor
public class ClientGovernanceController {

    private static final String RESOURCE_TYPE_OAUTH_CLIENT = "OAUTH_CLIENT";

    private final GovernancePublishService governancePublishService;
    private final GovernanceResourceQueryService governanceResourceQueryService;

    @GetMapping
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CLIENTS_VIEW)
    public R<PageResponse<GovernanceResourceDetailResponse>> listClients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        return R.ok(governanceResourceQueryService.list(GovernanceDomain.CLIENTS, RESOURCE_TYPE_OAUTH_CLIENT,
                keyword, offset, limit));
    }

    @GetMapping("/{resourceKey}")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CLIENTS_VIEW)
    public R<GovernanceResourceDetailResponse> clientDetail(@PathVariable String resourceKey) {
        return R.ok(governanceResourceQueryService.detail(GovernanceDomain.CLIENTS, RESOURCE_TYPE_OAUTH_CLIENT, resourceKey));
    }

    /**
     * 创建 OAuth Client 治理草稿。
     *
     * @param request 草稿创建请求
     * @return 治理变更响应
    */
    @PostMapping("/drafts")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CLIENTS_EDIT)
    public R<GovernanceChangeResponse> createClientDraft(
            @Valid @RequestBody GroupedGovernanceDraftCreateRequest request) {
        return governancePublishService.createDraft(GovernanceDraftRequestConverter.CONVERTER.mapToCreateRequest(
                request, GovernanceDomain.CLIENTS, RESOURCE_TYPE_OAUTH_CLIENT), currentOperatorId());
    }

    private String currentOperatorId() {
        return String.valueOf(AuthUtils.getCurrentUser().getId());
    }
}
