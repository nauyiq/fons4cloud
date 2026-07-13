package com.fons.cloud.admin.interfaces.rest.api;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.application.GovernanceDiffService;
import com.fons.cloud.admin.domain.codec.GovernanceConfigCodec;
import com.fons.cloud.admin.domain.codec.GovernanceConfigCodecRegistry;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.admin.interfaces.rest.api.model.GovernanceDiffRequest;
import com.fons.cloud.admin.interfaces.rest.api.model.GovernanceDiffResponse;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 服务端配置规范化、校验与语义差异 API。 */
@RestController
@RequestMapping("/admin/api/governance/config")
public class GovernanceConfigController {

    private final GovernanceConfigCodecRegistry codecRegistry;
    private final GovernanceDiffService diffService;

    public GovernanceConfigController(GovernanceConfigCodecRegistry codecRegistry, GovernanceDiffService diffService) {
        this.codecRegistry = codecRegistry;
        this.diffService = diffService;
    }

    @PostMapping("/diff")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.CHANGES_VIEW)
    public R<GovernanceDiffResponse> diff(@Valid @RequestBody GovernanceDiffRequest request) {
        GovernanceConfigCodec codec = codecRegistry.required(request.domain(), request.resourceType());
        List<GovernanceConfigCodec.ValidationIssue> issues = codec.validate(request.afterContent());
        if (!issues.isEmpty()) {
            return R.failed("AD200002", issues.getFirst().message());
        }
        return R.ok(diffService.diff(request.beforeContent(), request.afterContent(), codec));
    }
}
