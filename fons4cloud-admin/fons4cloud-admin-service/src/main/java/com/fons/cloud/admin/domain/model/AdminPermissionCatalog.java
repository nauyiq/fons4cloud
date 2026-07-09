package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.api.constants.AdminPermissionCodes;

import java.util.List;

/**
 * admin 第一版权限点目录。
 */
public final class AdminPermissionCatalog {

    private AdminPermissionCatalog() {
    }

    /**
     * 返回第一版固定权限点。
     *
     * @return 权限点定义集合
     */
    public static List<PermissionDefinition> definitions() {
        return List.of(
                define(AdminPermissionCodes.SERVICES_VIEW, "服务治理查看"),
                define(AdminPermissionCodes.GATEWAY_VIEW, "网关治理查看"),
                define(AdminPermissionCodes.GATEWAY_EDIT, "网关治理编辑草稿"),
                define(AdminPermissionCodes.GATEWAY_PUBLISH, "网关治理发布"),
                define(AdminPermissionCodes.GATEWAY_ROLLBACK, "网关治理回滚"),
                define(AdminPermissionCodes.TRAFFIC_VIEW, "流量治理查看"),
                define(AdminPermissionCodes.TRAFFIC_EDIT, "流量治理编辑草稿"),
                define(AdminPermissionCodes.TRAFFIC_PUBLISH, "流量治理发布"),
                define(AdminPermissionCodes.TRAFFIC_ROLLBACK, "流量治理回滚"),
                define(AdminPermissionCodes.ACCESS_VIEW, "身份与权限治理查看"),
                define(AdminPermissionCodes.ACCESS_EDIT, "身份与权限治理编辑"),
                define(AdminPermissionCodes.ACCESS_PUBLISH, "身份与权限治理发布"),
                define(AdminPermissionCodes.ACCESS_ROLLBACK, "身份与权限治理回滚"),
                define(AdminPermissionCodes.CLIENTS_VIEW, "认证客户端治理查看"),
                define(AdminPermissionCodes.CLIENTS_EDIT, "认证客户端治理编辑"),
                define(AdminPermissionCodes.CLIENTS_PUBLISH, "认证客户端治理发布"),
                define(AdminPermissionCodes.CLIENTS_ROLLBACK, "认证客户端治理回滚"),
                define(AdminPermissionCodes.OBSERVABILITY_VIEW, "可观测治理查看"),
                define(AdminPermissionCodes.CHANGES_VIEW, "变更治理查看"),
                define(AdminPermissionCodes.CHANGES_EDIT, "变更治理编辑草稿"),
                define(AdminPermissionCodes.CHANGES_PUBLISH, "变更治理发布"),
                define(AdminPermissionCodes.CHANGES_ROLLBACK, "变更治理回滚"),
                define(AdminPermissionCodes.AUDITS_VIEW, "审计查询查看")
        );
    }

    private static PermissionDefinition define(String permissionCode, String description) {
        String[] parts = permissionCode.split(":");
        return new PermissionDefinition(permissionCode, parts[0], parts[1], description);
    }

    /**
     * 权限点定义。
     *
     * @param permissionCode 权限编码
     * @param domain         治理域
     * @param action         操作类型
     * @param description    权限说明
     */
    public record PermissionDefinition(String permissionCode, String domain, String action, String description) {
    }
}
