package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.request.AuditQueryRequest;
import com.fons.cloud.admin.api.response.GovernanceAuditResponse;
import com.fons.cloud.admin.application.AuditApplicationService;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 审计查询 REST API。
 */
@RestController
@RequestMapping("/admin/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditApplicationService auditApplicationService;

    /**
     * 查询治理审计列表。
     *
     * @param request 查询条件
     * @return 审计列表
    */
    @GetMapping
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.AUDITS_VIEW)
    public R<List<GovernanceAuditResponse>> query(@ModelAttribute AuditQueryRequest request) {
        return auditApplicationService.query(request);
    }

    /**
     * 查询治理审计详情。
     *
     * @param id 审计记录 ID
     * @return 审计详情
    */
    @GetMapping("/{id}")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.AUDITS_VIEW)
    public R<GovernanceAuditResponse> getById(@PathVariable Long id) {
        return auditApplicationService.getById(id);
    }
}
