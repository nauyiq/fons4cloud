package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.AdminRoleType;
import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.admin.api.request.AdminRoleSaveRequest;
import com.fons.cloud.admin.api.request.AdminUserBindRequest;
import com.fons.cloud.admin.domain.entity.AdminPermission;
import com.fons.cloud.admin.domain.entity.AdminRolePermission;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * admin 管理员、角色和权限点应用服务。
 */
@Service
@RequiredArgsConstructor
public class AdminAccessApplicationService {

    private static final String SYSTEM_OPERATOR = "system";

    private final AdminAuthProperties adminAuthProperties;
    private final AdminAccountClient adminAccountClient;
    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;

    /**
     * 将认证服务账号绑定为 admin 管理员。
     *
     * @param request  绑定请求
     * @param operator 操作人账号或系统标识
     * @return 是否绑定成功
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> bindAdminUser(AdminUserBindRequest request, String operator) {
        R<AccountInfo> accountResult = adminAccountClient.queryById(request.getAccountId());
        if (rpcFailed(accountResult)) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }

        com.fons.cloud.admin.domain.model.AdminUser adminUserModel =
                new com.fons.cloud.admin.domain.model.AdminUser(accountResult.getData(), adminAuthProperties.getClientId());
        if (!adminUserModel.belongsToAdminClient()) {
            return R.failed(AdminResultCode.ADMIN_ACCOUNT_CLIENT_MISMATCH);
        }

        List<com.fons.cloud.admin.domain.entity.AdminRole> roles = findRoles(request.getRoleCodes());
        if (roles.size() != request.getRoleCodes().size()) {
            return R.failed(AdminResultCode.ADMIN_ROLE_NOT_FOUND);
        }

        com.fons.cloud.admin.domain.entity.AdminUser adminUser = findAdminUserByAccountId(request.getAccountId());
        if (adminUser == null) {
            adminUser = adminUserModel.toEntity(request.getDisplayName(), request.getDescription(), operator);
            adminUserMapper.insert(adminUser);
        } else {
            adminUser.setUsername(accountResult.getData().getUsername());
            adminUser.setDisplayName(request.getDisplayName());
            adminUser.setDescription(request.getDescription());
            adminUser.setStatus(AdminUserStatus.ACTIVE.name());
            adminUser.setUpdatedBy(operator);
            adminUserMapper.updateById(adminUser);
            adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>()
                    .eq(AdminUserRole::getAdminUserId, adminUser.getId()));
        }

        grantRoles(adminUser.getId(), roles, operator);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 禁用 admin 管理员，不修改认证服务账号状态。
     *
     * @param adminUserId admin 用户 ID
     * @param operator    操作人账号或系统标识
     * @return 是否禁用成功
     */
    public R<Boolean> disableAdminUser(Long adminUserId, String operator) {
        com.fons.cloud.admin.domain.entity.AdminUser adminUser = adminUserMapper.selectById(adminUserId);
        if (adminUser == null) {
            return R.failed(AdminResultCode.ADMIN_USER_NOT_BOUND);
        }
        adminUser.setStatus(AdminUserStatus.DISABLED.name());
        adminUser.setUpdatedBy(operator);
        return R.ok(adminUserMapper.updateById(adminUser) > 0);
    }

    /**
     * 新增或更新 admin 角色，并重建角色权限点关系。
     *
     * @param request  角色保存请求
     * @param operator 操作人账号或系统标识
     * @return 是否保存成功
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> saveRole(AdminRoleSaveRequest request, String operator) {
        Set<String> permissionCodes = request.getPermissionCodes();
        List<AdminPermission> permissions = findPermissions(permissionCodes);
        if (permissionCodes != null && permissions.size() != permissionCodes.size()) {
            return R.failed(AdminResultCode.ADMIN_PERMISSION_NOT_FOUND);
        }

        com.fons.cloud.admin.domain.entity.AdminRole role = findRole(request.getRoleCode());
        if (role == null) {
            role = new com.fons.cloud.admin.domain.entity.AdminRole();
            role.setRoleCode(request.getRoleCode());
            role.setRoleType(AdminRoleType.CUSTOM.name());
        }
        role.setRoleName(request.getRoleName());
        role.setStatus((request.getStatus() == null ? AdminUserStatus.ACTIVE : request.getStatus()).name());
        role.setDescription(request.getDescription());

        if (role.getId() == null) {
            adminRoleMapper.insert(role);
        } else {
            adminRoleMapper.updateById(role);
            adminRolePermissionMapper.delete(new LambdaQueryWrapper<AdminRolePermission>()
                    .eq(AdminRolePermission::getRoleId, role.getId()));
        }

        grantPermissions(role.getId(), permissions, operator);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 初始化固定权限点目录。
     *
     * @return 初始化或补齐的权限点数量
     */
    public int initializePermissions() {
        int initialized = 0;
        for (AdminPermissionCatalog.PermissionDefinition definition : AdminPermissionCatalog.definitions()) {
            AdminPermission exists = findPermission(definition.permissionCode());
            if (exists != null) {
                continue;
            }
            AdminPermission permission = new AdminPermission();
            permission.setPermissionCode(definition.permissionCode());
            permission.setDomain(definition.domain());
            permission.setAction(definition.action());
            permission.setDescription(definition.description());
            permission.setStatus(AdminUserStatus.ACTIVE.name());
            adminPermissionMapper.insert(permission);
            initialized++;
        }
        return initialized;
    }

    /**
     * 初始化或补齐内置 ROOT 角色。
     *
     * @return ROOT 角色实体
     */
    @Transactional(rollbackFor = Exception.class)
    public com.fons.cloud.admin.domain.entity.AdminRole initializeRootRole() {
        initializePermissions();
        com.fons.cloud.admin.domain.entity.AdminRole role = findRole(com.fons.cloud.admin.domain.model.AdminRole.ROOT_ROLE_CODE);
        if (role == null) {
            role = com.fons.cloud.admin.domain.model.AdminRole.rootRole();
            adminRoleMapper.insert(role);
        }

        List<AdminPermission> permissions = findPermissions(AdminPermissionCatalog.definitions().stream()
                .map(AdminPermissionCatalog.PermissionDefinition::permissionCode)
                .collect(Collectors.toSet()));
        adminRolePermissionMapper.delete(new LambdaQueryWrapper<AdminRolePermission>()
                .eq(AdminRolePermission::getRoleId, role.getId()));
        grantPermissions(role.getId(), permissions, SYSTEM_OPERATOR);
        return role;
    }

    /**
     * 初始化首个 ROOT 管理员。
     *
     * @param accountInfo ROOT 认证账号
     * @return 是否初始化成功
     */
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> initializeRootAdmin(AccountInfo accountInfo) {
        if (accountInfo == null) {
            return R.failed(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        com.fons.cloud.admin.domain.model.AdminUser adminUserModel =
                new com.fons.cloud.admin.domain.model.AdminUser(accountInfo, adminAuthProperties.getClientId());
        if (!adminUserModel.belongsToAdminClient()) {
            return R.failed(AdminResultCode.ADMIN_ACCOUNT_CLIENT_MISMATCH);
        }
        if (adminUserMapper.selectCount(null) > 0) {
            return R.ok(Boolean.FALSE);
        }

        initializeRootRole();
        AdminUserBindRequest request = AdminUserBindRequest.builder()
                .accountId(accountInfo.getId())
                .username(accountInfo.getUsername())
                .displayName(accountInfo.getUsername())
                .roleCodes(Set.of(com.fons.cloud.admin.domain.model.AdminRole.ROOT_ROLE_CODE))
                .description("首个 ROOT 管理员初始化")
                .build();
        return bindAdminUser(request, SYSTEM_OPERATOR);
    }

    private void grantRoles(Long adminUserId, Collection<com.fons.cloud.admin.domain.entity.AdminRole> roles, String operator) {
        for (com.fons.cloud.admin.domain.entity.AdminRole role : roles) {
            AdminUserRole userRole = new AdminUserRole();
            userRole.setAdminUserId(adminUserId);
            userRole.setRoleId(role.getId());
            userRole.setGrantedBy(operator);
            adminUserRoleMapper.insert(userRole);
        }
    }

    private void grantPermissions(Long roleId, Collection<AdminPermission> permissions, String operator) {
        for (AdminPermission permission : permissions) {
            AdminRolePermission rolePermission = new AdminRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setGrantedBy(operator);
            adminRolePermissionMapper.insert(rolePermission);
        }
    }

    private com.fons.cloud.admin.domain.entity.AdminUser findAdminUserByAccountId(Long accountId) {
        return adminUserMapper.selectOne(new LambdaQueryWrapper<com.fons.cloud.admin.domain.entity.AdminUser>()
                .eq(com.fons.cloud.admin.domain.entity.AdminUser::getAccountId, accountId));
    }

    private List<com.fons.cloud.admin.domain.entity.AdminRole> findRoles(Set<String> roleCodes) {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<com.fons.cloud.admin.domain.entity.AdminRole>()
                .in(com.fons.cloud.admin.domain.entity.AdminRole::getRoleCode, roleCodes)
                .eq(com.fons.cloud.admin.domain.entity.AdminRole::getStatus, AdminUserStatus.ACTIVE.name()));
    }

    private com.fons.cloud.admin.domain.entity.AdminRole findRole(String roleCode) {
        return adminRoleMapper.selectOne(new LambdaQueryWrapper<com.fons.cloud.admin.domain.entity.AdminRole>()
                .eq(com.fons.cloud.admin.domain.entity.AdminRole::getRoleCode, roleCode));
    }

    private List<AdminPermission> findPermissions(Set<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        return adminPermissionMapper.selectList(new LambdaQueryWrapper<AdminPermission>()
                .in(AdminPermission::getPermissionCode, permissionCodes)
                .eq(AdminPermission::getStatus, AdminUserStatus.ACTIVE.name()));
    }

    private AdminPermission findPermission(String permissionCode) {
        return adminPermissionMapper.selectOne(new LambdaQueryWrapper<AdminPermission>()
                .eq(AdminPermission::getPermissionCode, permissionCode));
    }

    private boolean rpcFailed(R<?> rpcResult) {
        return rpcResult == null || !rpcResult.isSuccess() || Objects.isNull(rpcResult.getData());
    }
}
