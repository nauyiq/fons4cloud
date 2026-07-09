package com.fons.cloud.admin.interfaces.rest;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.request.ActuatorProbeRequest;
import com.fons.cloud.admin.api.response.ActuatorProbeResult;
import com.fons.cloud.admin.infrastructure.actuator.ActuatorReadAdapter;
import com.fons.cloud.admin.infrastructure.security.AdminPermission;
import com.fons.cloud.auth.annotation.AuthenticationResource;
import com.fons.cloud.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可观测治理只读 REST API。
 */
@RestController
@RequestMapping("/admin/observability")
@RequiredArgsConstructor
public class ObservabilityController {

    private final ActuatorReadAdapter actuatorReadAdapter;

    /**
     * 执行 Actuator 只读探测。
     *
     * @param request 探测请求
     * @return 探测结果
    */
    @PostMapping("/actuator/probe")
    @AuthenticationResource(authorities = "ADMIN")
    @AdminPermission(authorities = AdminPermissionCodes.OBSERVABILITY_VIEW)
    public R<ActuatorProbeResult> probe(@Valid @RequestBody ActuatorProbeRequest request) {
        return R.ok(actuatorReadAdapter.probe(request));
    }
}
