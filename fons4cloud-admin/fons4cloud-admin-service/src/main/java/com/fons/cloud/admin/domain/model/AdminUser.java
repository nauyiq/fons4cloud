package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.auth.response.AccountInfo;

/**
 * admin 管理员绑定领域规则。
 */
public class AdminUser {

    private final AccountInfo accountInfo;

    private final String adminClientId;

    public AdminUser(AccountInfo accountInfo, String adminClientId) {
        this.accountInfo = accountInfo;
        this.adminClientId = adminClientId;
    }

    /**
     * 判断认证账号是否属于统一控制面客户端。
     *
     * @return 是否允许绑定为 admin 用户
     */
    public boolean belongsToAdminClient() {
        return accountInfo != null && adminClientId != null && adminClientId.equals(accountInfo.getClientId());
    }

    /**
     * 创建管理员绑定实体。
     *
     * @param displayName 展示名称
     * @param description 绑定说明
     * @param operator    操作人
     * @return admin 用户持久化实体
     */
    public com.fons.cloud.admin.domain.entity.AdminUser toEntity(String displayName, String description, String operator) {
        com.fons.cloud.admin.domain.entity.AdminUser adminUser = new com.fons.cloud.admin.domain.entity.AdminUser();
        adminUser.setAccountId(accountInfo.getId());
        adminUser.setUsername(accountInfo.getUsername());
        adminUser.setDisplayName(displayName);
        adminUser.setStatus(AdminUserStatus.ACTIVE.name());
        adminUser.setDescription(description);
        adminUser.setCreatedBy(operator);
        adminUser.setUpdatedBy(operator);
        return adminUser;
    }
}
