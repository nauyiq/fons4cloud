package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.enums.AdminRoleType;
import com.fons.cloud.admin.api.enums.AdminUserStatus;

/**
 * admin 角色领域规则。
 */
public class AdminRole {

    public static final String ROOT_ROLE_CODE = "ADMIN_ROOT";

    /**
     * 创建 ROOT 角色实体。
     *
     * @return ROOT 角色持久化实体
     */
    public static com.fons.cloud.admin.domain.entity.AdminRole rootRole() {
        com.fons.cloud.admin.domain.entity.AdminRole role = new com.fons.cloud.admin.domain.entity.AdminRole();
        role.setRoleCode(ROOT_ROLE_CODE);
        role.setRoleName("超级管理员");
        role.setRoleType(AdminRoleType.BUILT_IN.name());
        role.setStatus(AdminUserStatus.ACTIVE.name());
        role.setDescription("控制面内置超级管理员角色");
        return role;
    }
}
