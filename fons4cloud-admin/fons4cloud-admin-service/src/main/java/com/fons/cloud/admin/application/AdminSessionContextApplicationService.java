package com.fons.cloud.admin.application;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.infrastructure.config.AdminEnvironmentProperties;
import com.fons.cloud.admin.infrastructure.security.AdminAuthorizationService;
import com.fons.cloud.admin.interfaces.rest.api.model.AdminSessionContextResponse;
import com.fons.cloud.auth.api.AuthUser;
import com.fons.cloud.common.result.R;
import org.springframework.stereotype.Service;

import java.util.Map;

/** 组装当前管理员可安全返回给浏览器的会话上下文。 */
@Service
public class AdminSessionContextApplicationService {

    private final AdminAuthorizationService authorizationService;
    private final AdminEnvironmentProperties environmentProperties;

    public AdminSessionContextApplicationService(AdminAuthorizationService authorizationService,
                                                 AdminEnvironmentProperties environmentProperties) {
        this.authorizationService = authorizationService;
        this.environmentProperties = environmentProperties;
    }

    public R<AdminSessionContextResponse> context(AuthUser authUser) {
        AdminAuthorizationService.AdminSessionAccess access = authorizationService.loadSessionAccess(authUser);
        if (access == null) {
            return R.failed(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        if (!access.active()) {
            return R.failed(AdminResultCode.ADMIN_USER_DISABLED);
        }
        return R.ok(new AdminSessionContextResponse(access.adminUserId(), access.username(),
                environmentProperties.getName(), access.permissionCodes(), Map.of(
                "discovery", AdminSessionContextResponse.DependencyState.UNKNOWN,
                "nacos", AdminSessionContextResponse.DependencyState.UNKNOWN,
                "redis", AdminSessionContextResponse.DependencyState.UNKNOWN,
                "auth", AdminSessionContextResponse.DependencyState.UNKNOWN)));
    }
}
