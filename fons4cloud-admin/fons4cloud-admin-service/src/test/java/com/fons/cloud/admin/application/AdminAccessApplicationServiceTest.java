package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.admin.api.request.AdminUserBindRequest;
import com.fons.cloud.admin.domain.entity.AdminPermission;
import com.fons.cloud.admin.domain.entity.AdminRole;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.admin.domain.entity.AdminUserRole;
import com.fons.cloud.admin.domain.mapper.AdminPermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminRoleMapper;
import com.fons.cloud.admin.domain.mapper.AdminRolePermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserRoleMapper;
import com.fons.cloud.admin.domain.model.AdminPermissionCatalog;
import com.fons.cloud.admin.infrastructure.auth.AdminAccountClient;
import com.fons.cloud.admin.infrastructure.auth.AdminAuthProperties;
import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * admin 管理员和角色权限应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminAccessApplicationServiceTest {

    @Mock
    private AdminAccountClient adminAccountClient;
    @Mock
    private AdminUserMapper adminUserMapper;
    @Mock
    private AdminRoleMapper adminRoleMapper;
    @Mock
    private AdminPermissionMapper adminPermissionMapper;
    @Mock
    private AdminUserRoleMapper adminUserRoleMapper;
    @Mock
    private AdminRolePermissionMapper adminRolePermissionMapper;

    private AdminAccessApplicationService adminAccessApplicationService;

    @BeforeEach
    void setUp() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setClientId("sys-admin");
        adminAccessApplicationService = new AdminAccessApplicationService(properties, adminAccountClient,
                adminUserMapper, adminRoleMapper, adminPermissionMapper, adminUserRoleMapper, adminRolePermissionMapper);
    }

    @Test
    void bindAdminUserShouldRejectAccountFromOtherClient() {
        when(adminAccountClient.queryById(100L)).thenReturn(R.ok(account(100L, "biz-user", "BIZ")));

        R<Boolean> response = adminAccessApplicationService.bindAdminUser(bindRequest(), "operator");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(AdminResultCode.ADMIN_ACCOUNT_CLIENT_MISMATCH.getCode());
        verify(adminUserMapper, never()).insert(any(AdminUser.class));
    }

    @Test
    void bindAdminUserShouldCreateBindingAndGrantRolesForAdminClientAccount() {
        AdminRole rootRole = role(1L, "ADMIN_ROOT");
        when(adminAccountClient.queryById(100L)).thenReturn(R.ok(account(100L, "root", "sys-admin")));
        when(adminRoleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(rootRole));
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(adminUserMapper.insert(any(AdminUser.class))).thenAnswer(invocation -> {
            AdminUser adminUser = invocation.getArgument(0);
            adminUser.setId(10L);
            return 1;
        });

        R<Boolean> response = adminAccessApplicationService.bindAdminUser(bindRequest(), "operator");

        assertThat(response.isSuccess()).isTrue();

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getAccountId()).isEqualTo(100L);
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("root");
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(AdminUserStatus.ACTIVE.name());

        ArgumentCaptor<AdminUserRole> userRoleCaptor = ArgumentCaptor.forClass(AdminUserRole.class);
        verify(adminUserRoleMapper).insert(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getAdminUserId()).isEqualTo(10L);
        assertThat(userRoleCaptor.getValue().getRoleId()).isEqualTo(1L);
        assertThat(userRoleCaptor.getValue().getGrantedBy()).isEqualTo("operator");
    }

    @Test
    void disableAdminUserShouldOnlyDisableAdminBinding() {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(10L);
        adminUser.setStatus(AdminUserStatus.ACTIVE.name());
        when(adminUserMapper.selectById(10L)).thenReturn(adminUser);
        when(adminUserMapper.updateById(adminUser)).thenReturn(1);

        R<Boolean> response = adminAccessApplicationService.disableAdminUser(10L, "operator");

        assertThat(response.isSuccess()).isTrue();
        assertThat(adminUser.getStatus()).isEqualTo(AdminUserStatus.DISABLED.name());
        verify(adminAccountClient, never()).queryById(any());
    }

    @Test
    void initializePermissionsShouldInsertMissingPermissionCatalog() {
        when(adminPermissionMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        int initialized = adminAccessApplicationService.initializePermissions();

        assertThat(initialized).isEqualTo(AdminPermissionCatalog.definitions().size());
        verify(adminPermissionMapper, times(AdminPermissionCatalog.definitions().size())).insert(any(AdminPermission.class));
    }

    @Test
    void initializeRootRoleShouldCreateBuiltInRootRoleAndGrantAllPermissions() {
        List<AdminPermission> permissions = List.of(
                permission(1L, AdminPermissionCodes.ACCESS_VIEW),
                permission(2L, AdminPermissionCodes.AUDITS_VIEW)
        );
        when(adminPermissionMapper.selectOne(any(Wrapper.class))).thenReturn(permission(1L, AdminPermissionCodes.ACCESS_VIEW));
        when(adminRoleMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(adminRoleMapper.insert(any(AdminRole.class))).thenAnswer(invocation -> {
            AdminRole role = invocation.getArgument(0);
            role.setId(99L);
            return 1;
        });
        when(adminPermissionMapper.selectList(any(Wrapper.class))).thenReturn(permissions);

        AdminRole rootRole = adminAccessApplicationService.initializeRootRole();

        assertThat(rootRole.getRoleCode()).isEqualTo("ADMIN_ROOT");
        assertThat(rootRole.getId()).isEqualTo(99L);
        verify(adminRolePermissionMapper).delete(any(Wrapper.class));
        verify(adminRolePermissionMapper, times(2)).insert(any());
    }

    private AdminUserBindRequest bindRequest() {
        return AdminUserBindRequest.builder()
                .accountId(100L)
                .displayName("Root")
                .roleCodes(Set.of("ADMIN_ROOT"))
                .description("root binding")
                .build();
    }

    private AccountInfo account(Long id, String username, String clientId) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setId(id);
        accountInfo.setUsername(username);
        accountInfo.setClientId(clientId);
        return accountInfo;
    }

    private AdminRole role(Long id, String roleCode) {
        AdminRole role = new AdminRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setStatus(AdminUserStatus.ACTIVE.name());
        return role;
    }

    private AdminPermission permission(Long id, String permissionCode) {
        AdminPermission permission = new AdminPermission();
        permission.setId(id);
        permission.setPermissionCode(permissionCode);
        permission.setStatus(AdminUserStatus.ACTIVE.name());
        return permission;
    }
}
