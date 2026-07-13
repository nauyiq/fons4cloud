package com.fons.cloud.admin.interfaces.rest.api;

import com.fons.cloud.admin.application.AdminSessionContextApplicationService;
import com.fons.cloud.admin.interfaces.rest.api.model.AdminSessionContextResponse;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vue 应用壳使用的当前会话上下文。 */
@RestController
@RequestMapping("/admin/api/session")
public class AdminSessionController {

    private final AdminSessionContextApplicationService applicationService;

    public AdminSessionController(AdminSessionContextApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/context")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = {})
    public R<AdminSessionContextResponse> context() {
        return applicationService.context(AuthUtils.getCurrentUser());
    }
}
