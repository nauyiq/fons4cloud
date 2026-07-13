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
 * 流量治理 REST API。
 */
@RestController
@RequestMapping("/admin/traffic")
@RequiredArgsConstructor
public class TrafficGovernanceController {

    private static final String RESOURCE_TYPE_IP_LIST = "IP_LIST";

    private final GovernancePublishService governancePublishService;
    private final GovernanceResourceQueryService governanceResourceQueryService;

    @GetMapping("/ip-lists")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.TRAFFIC_VIEW)
    public R<PageResponse<GovernanceResourceDetailResponse>> listIpLists(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        return R.ok(governanceResourceQueryService.list(GovernanceDomain.TRAFFIC, RESOURCE_TYPE_IP_LIST,
                keyword, offset, limit));
    }

    @GetMapping("/ip-lists/{resourceKey}")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.TRAFFIC_VIEW)
    public R<GovernanceResourceDetailResponse> ipListDetail(@PathVariable String resourceKey) {
        return R.ok(governanceResourceQueryService.detail(GovernanceDomain.TRAFFIC, RESOURCE_TYPE_IP_LIST, resourceKey));
    }

    /**
     * 创建 IP 黑白名单治理草稿。
     *
     * @param request 草稿创建请求
     * @return 治理变更响应
    */
    @PostMapping("/ip-lists/drafts")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.TRAFFIC_EDIT)
    public R<GovernanceChangeResponse> createIpListDraft(
            @Valid @RequestBody GroupedGovernanceDraftCreateRequest request) {
        return governancePublishService.createDraft(GovernanceDraftRequestConverter.CONVERTER.mapToCreateRequest(
                request, GovernanceDomain.TRAFFIC, RESOURCE_TYPE_IP_LIST), currentOperatorId());
    }

    private String currentOperatorId() {
        return String.valueOf(AuthUtils.getCurrentUser().getId());
    }
}
