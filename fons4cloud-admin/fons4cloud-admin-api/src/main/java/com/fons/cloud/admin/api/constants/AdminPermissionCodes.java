package com.fons.cloud.admin.api.constants;

/**
 * admin 第一版权限点编码，格式固定为 {domain}:{action}。
 */
public interface AdminPermissionCodes {

    String SERVICES_VIEW = "services:view";
    String GATEWAY_VIEW = "gateway:view";
    String GATEWAY_EDIT = "gateway:edit";
    String GATEWAY_PUBLISH = "gateway:publish";
    String GATEWAY_ROLLBACK = "gateway:rollback";
    String TRAFFIC_VIEW = "traffic:view";
    String TRAFFIC_EDIT = "traffic:edit";
    String TRAFFIC_PUBLISH = "traffic:publish";
    String TRAFFIC_ROLLBACK = "traffic:rollback";
    String ACCESS_VIEW = "access:view";
    String ACCESS_EDIT = "access:edit";
    String ACCESS_PUBLISH = "access:publish";
    String ACCESS_ROLLBACK = "access:rollback";
    String CLIENTS_VIEW = "clients:view";
    String CLIENTS_EDIT = "clients:edit";
    String CLIENTS_PUBLISH = "clients:publish";
    String CLIENTS_ROLLBACK = "clients:rollback";
    String OBSERVABILITY_VIEW = "observability:view";
    String CHANGES_VIEW = "changes:view";
    String CHANGES_EDIT = "changes:edit";
    String CHANGES_PUBLISH = "changes:publish";
    String CHANGES_ROLLBACK = "changes:rollback";
    String AUDITS_VIEW = "audits:view";
}
