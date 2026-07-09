package com.fons.cloud.admin.infrastructure.config;

import com.fons.cloud.admin.application.AdminAccessApplicationService;
import com.fons.cloud.admin.infrastructure.auth.AdminAccountClient;
import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.common.result.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * admin 权限点目录、ROOT 角色和首个 ROOT 管理员初始化器。
 */
@Component
@RequiredArgsConstructor
public class AdminAccessInitializer {

    private final AdminAccessProperties properties;
    private final AdminAccountClient adminAccountClient;
    private final AdminAccessApplicationService adminAccessApplicationService;

    /**
     * 应用启动完成后补齐权限目录和可选 ROOT 管理员。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        adminAccessApplicationService.initializePermissions();
        if (!properties.isRootInitializerEnabled()) {
            return;
        }

        AccountInfo rootAccount = loadRootAccount();
        if (rootAccount != null) {
            adminAccessApplicationService.initializeRootAdmin(rootAccount);
        }
    }

    private AccountInfo loadRootAccount() {
        R<AccountInfo> accountResult = null;
        if (properties.getRootAccountId() != null) {
            accountResult = adminAccountClient.queryById(properties.getRootAccountId());
        } else if (StringUtils.isNotBlank(properties.getRootUsername())) {
            accountResult = adminAccountClient.queryByUsername(properties.getRootUsername());
        }
        return accountResult != null && accountResult.isSuccess() ? accountResult.getData() : null;
    }
}
