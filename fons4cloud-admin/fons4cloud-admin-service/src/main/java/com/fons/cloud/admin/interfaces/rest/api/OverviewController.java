package com.fons.cloud.admin.interfaces.rest.api;

import com.fons.cloud.admin.application.OverviewApplicationService;
import com.fons.cloud.admin.interfaces.rest.api.model.OverviewResponse;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 运行概览 API。 */
@RestController
@RequestMapping("/admin/api/overview")
public class OverviewController {

    private final OverviewApplicationService applicationService;

    public OverviewController(OverviewApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @AuthenticationResource(authorities = "ADMIN")
    public R<OverviewResponse> overview() {
        return applicationService.overview(AuthUtils.getCurrentUser());
    }
}
