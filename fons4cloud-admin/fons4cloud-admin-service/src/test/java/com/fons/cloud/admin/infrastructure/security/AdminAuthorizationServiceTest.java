package com.fons.cloud.admin.infrastructure.security;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.admin.domain.entity.AdminPermission;
import com.fons.cloud.admin.domain.entity.AdminRole;
import com.fons.cloud.admin.domain.entity.AdminRolePermission;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.admin.domain.entity.AdminUserRole;
import com.fons.cloud.admin.domain.mapper.AdminPermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminRoleMapper;
import com.fons.cloud.admin.domain.mapper.AdminRolePermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserRoleMapper;
import com.fons.cloud.auth.api.AuthUser;
import com.fons.cloud.auth.api.support.DefaultAuthUser;
import com.fons.cloud.auth.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * admin RBAC 运行期授权服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;
    @Mock
    private AdminUserRoleMapper adminUserRoleMapper;
    @Mock
    private AdminRoleMapper adminRoleMapper;
    @Mock
    private AdminRolePermissionMapper adminRolePermissionMapper;
    @Mock
    private AdminPermissionMapper adminPermissionMapper;

    private AdminAuthorizationService adminAuthorizationService;

    @BeforeEach
    void setUp() {
        adminAuthorizationService = new AdminAuthorizationService(adminUserMapper, adminUserRoleMapper, adminRoleMapper,
                adminRolePermissionMapper, adminPermissionMapper);
    }

    @Test
    void authorizeShouldRejectUnboundAccount() {
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        AdminAuthorizationDecision decision = adminAuthorizationService.authorize(authUser(), Set.of(AdminPermissionCodes.ACCESS_VIEW));

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDeniedResult()).isEqualTo(AdminResultCode.ADMIN_USER_NOT_BOUND);
    }

    @Test
    void authorizeShouldRejectDisabledAdminUser() {
        AdminUser adminUser = adminUser(AdminUserStatus.DISABLED.name());
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(adminUser);

        AdminAuthorizationDecision decision = adminAuthorizationService.authorize(authUser(), Set.of(AdminPermissionCodes.ACCESS_VIEW));

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDeniedResult()).isEqualTo(AdminResultCode.ADMIN_USER_DISABLED);
    }

    @Test
    void authorizeShouldRejectMissingPermission() {
        mockActiveAdminWithPermissions(List.of(AdminPermissionCodes.SERVICES_VIEW));

        AdminAuthorizationDecision decision = adminAuthorizationService.authorize(authUser(), Set.of(AdminPermissionCodes.ACCESS_EDIT));

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDeniedResult()).isEqualTo(AdminResultCode.ADMIN_PERMISSION_DENIED);
    }

    @Test
    void authorizeShouldAllowWhenAnyRequiredPermissionMatched() {
        mockActiveAdminWithPermissions(List.of(AdminPermissionCodes.ACCESS_VIEW));

        AdminAuthorizationDecision decision = adminAuthorizationService.authorize(authUser(), Set.of(AdminPermissionCodes.ACCESS_VIEW));

        assertThat(decision.isAllowed()).isTrue();
    }

    private void mockActiveAdminWithPermissions(List<String> permissionCodes) {
        AdminUser adminUser = adminUser(AdminUserStatus.ACTIVE.name());
        when(adminUserMapper.selectOne(any(Wrapper.class))).thenReturn(adminUser);

        AdminUserRole userRole = new AdminUserRole();
        userRole.setRoleId(20L);
        when(adminUserRoleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(userRole));

        AdminRole role = new AdminRole();
        role.setId(20L);
        role.setStatus(AdminUserStatus.ACTIVE.name());
        when(adminRoleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(role));

        AdminRolePermission rolePermission = new AdminRolePermission();
        rolePermission.setPermissionId(30L);
        when(adminRolePermissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(rolePermission));

        when(adminPermissionMapper.selectList(any(Wrapper.class))).thenReturn(permissionCodes.stream()
                .map(this::permission)
                .toList());
    }

    private AdminUser adminUser(String status) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(10L);
        adminUser.setAccountId(1L);
        adminUser.setStatus(status);
        return adminUser;
    }

    private AdminPermission permission(String permissionCode) {
        AdminPermission permission = new AdminPermission();
        permission.setId(30L);
        permission.setPermissionCode(permissionCode);
        permission.setStatus(AdminUserStatus.ACTIVE.name());
        return permission;
    }

    private AuthUser authUser() {
        return new DefaultAuthUser(1L, "root", "root@example.com", "13800000000", UserRole.ADMIN, List.of());
    }
}
