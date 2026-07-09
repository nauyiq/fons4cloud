package com.fons.cloud.admin.infrastructure.config;

import com.fons.cloud.admin.application.AdminAccessApplicationService;
import com.fons.cloud.admin.infrastructure.auth.AdminAccountClient;
import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * admin 访问控制初始化器测试。
 */
class AdminAccessInitializerTest {

    @Test
    void initializeShouldLoadRootAccountByUsernameWhenEnabled() {
        AdminAccessProperties properties = new AdminAccessProperties();
        properties.setRootInitializerEnabled(true);
        properties.setRootUsername("root");
        AdminAccountClient accountClient = mock(AdminAccountClient.class);
        AdminAccessApplicationService accessApplicationService = mock(AdminAccessApplicationService.class);
        AccountInfo root = new AccountInfo();
        root.setId(100L);
        root.setUsername("root");
        root.setClientId("sys-admin");
        when(accountClient.queryByUsername("root")).thenReturn(R.ok(root));

        new AdminAccessInitializer(properties, accountClient, accessApplicationService).initialize();

        verify(accessApplicationService).initializePermissions();
        verify(accountClient).queryByUsername("root");
        verify(accessApplicationService).initializeRootAdmin(root);
    }
}
