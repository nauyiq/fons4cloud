package com.fons.cloud.admin.infrastructure.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * admin 运行期 RBAC 鉴权服务，基于 admin 自有授权表判断当前账号是否可访问目标 API。
 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final AdminPermissionMapper adminPermissionMapper;

    /**
     * 校验当前认证账号是否为有效 admin 管理员，并且至少拥有一个目标权限点。
     *
     * @param authUser 当前认证用户，由网关按 `AUTH_USER` 头传递
     * @param requiredPermissionCodes 目标 API 声明的 admin 权限点
     * @return admin 鉴权结果
     */
    public AdminAuthorizationDecision authorize(AuthUser authUser, Collection<String> requiredPermissionCodes) {
        AdminSessionAccess sessionAccess = loadSessionAccess(authUser);
        if (sessionAccess == null) {
            return AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        if (!sessionAccess.active()) {
            return AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_USER_DISABLED);
        }
        if (CollectionUtils.isEmpty(requiredPermissionCodes)) {
            return AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_PERMISSION_DENIED);
        }
        boolean permitted = requiredPermissionCodes.stream().anyMatch(sessionAccess.permissionCodes()::contains);
        return permitted ? AdminAuthorizationDecision.allow()
                : AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_PERMISSION_DENIED);
    }

    /**
     * 加载当前认证账号在 admin 自有 RBAC 中的会话视图。
     *
     * @param authUser 网关传递的认证用户
     * @return admin 用户和权限；未绑定时返回 null
     */
    public AdminSessionAccess loadSessionAccess(AuthUser authUser) {
        if (authUser == null) {
            return null;
        }
        AdminUser adminUser = findActiveBinding(authUser.getId());
        if (adminUser == null) {
            return null;
        }
        boolean active = AdminUserStatus.ACTIVE.name().equals(adminUser.getStatus());
        Set<String> permissionCodes = active ? loadOwnedPermissionCodes(adminUser.getId()) : Set.of();
        return new AdminSessionAccess(adminUser.getId(), adminUser.getUsername(), active, permissionCodes);
    }

    public record AdminSessionAccess(Long adminUserId, String username, boolean active, Set<String> permissionCodes) {
        public AdminSessionAccess {
            permissionCodes = Set.copyOf(permissionCodes);
        }
    }

    /**
     * 仅校验当前认证账号已绑定并启用，供会话上下文等不归属具体功能域的入口使用。
     *
     * @param authUser 网关传递的认证用户
     * @return admin 绑定状态校验结果
     */
    public AdminAuthorizationDecision authorizeAdmin(AuthUser authUser) {
        AdminSessionAccess sessionAccess = loadSessionAccess(authUser);
        if (sessionAccess == null) {
            return AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        return sessionAccess.active() ? AdminAuthorizationDecision.allow()
                : AdminAuthorizationDecision.deny(AdminResultCode.ADMIN_USER_DISABLED);
    }

    private AdminUser findActiveBinding(Long accountId) {
        return adminUserMapper.selectOne(Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getAccountId, accountId));
    }

    private Set<String> loadOwnedPermissionCodes(Long adminUserId) {
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectList(Wrappers.<AdminUserRole>lambdaQuery()
                .eq(AdminUserRole::getAdminUserId, adminUserId));
        if (CollectionUtils.isEmpty(userRoles)) {
            return Set.of();
        }
        Set<Long> roleIds = userRoles.stream().map(AdminUserRole::getRoleId).collect(Collectors.toSet());
        List<AdminRole> activeRoles = adminRoleMapper.selectList(Wrappers.<AdminRole>lambdaQuery()
                .in(AdminRole::getId, roleIds)
                .eq(AdminRole::getStatus, AdminUserStatus.ACTIVE.name()));
        if (CollectionUtils.isEmpty(activeRoles)) {
            return Set.of();
        }
        Set<Long> activeRoleIds = activeRoles.stream().map(AdminRole::getId).collect(Collectors.toSet());
        List<AdminRolePermission> rolePermissions = adminRolePermissionMapper.selectList(Wrappers.<AdminRolePermission>lambdaQuery()
                .in(AdminRolePermission::getRoleId, activeRoleIds));
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return Set.of();
        }
        Set<Long> permissionIds = rolePermissions.stream().map(AdminRolePermission::getPermissionId).collect(Collectors.toSet());
        List<AdminPermission> permissions = adminPermissionMapper.selectList(Wrappers.<AdminPermission>lambdaQuery()
                .in(AdminPermission::getId, permissionIds)
                .eq(AdminPermission::getStatus, AdminUserStatus.ACTIVE.name()));
        if (CollectionUtils.isEmpty(permissions)) {
            return Set.of();
        }
        return permissions.stream().map(AdminPermission::getPermissionCode).collect(Collectors.toSet());
    }
}
