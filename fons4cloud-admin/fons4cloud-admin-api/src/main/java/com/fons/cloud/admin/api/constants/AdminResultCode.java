package com.fons.cloud.admin.api.constants;

import com.fons.cloud.common.result.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * admin 控制面业务状态码。
 */
@Getter
@AllArgsConstructor
public enum AdminResultCode implements Result {

    ADMIN_USER_NOT_BOUND("AD100001", "当前账号未纳入admin管理体系"),
    ADMIN_USER_DISABLED("AD100002", "当前管理员已被禁用"),
    ADMIN_ACCOUNT_CLIENT_MISMATCH("AD100003", "账号不属于统一admin认证客户端"),
    ADMIN_AUTH_RPC_FAILED("AD100004", "认证服务RPC调用失败"),
    ADMIN_PERMISSION_DENIED("AD100005", "admin操作权限不足"),
    ADMIN_ROLE_NOT_FOUND("AD100006", "admin角色不存在"),
    ADMIN_PERMISSION_NOT_FOUND("AD100007", "admin权限点不存在"),
    ADMIN_REFRESH_COOKIE_INVALID("AD100008", "admin刷新会话已失效"),
    ADMIN_DRAFT_NOT_EDITABLE("AD200001", "当前草稿状态不允许编辑"),
    ADMIN_VALIDATION_FAILED("AD200002", "治理配置校验失败"),
    ADMIN_CONFIG_DRIFT_DETECTED("AD200003", "目标配置已发生外部漂移"),
    ADMIN_TARGET_UNAVAILABLE("AD200004", "治理目标暂不可用"),
    ADMIN_PUBLISH_CONFIRM_FAILED("AD200005", "发布后确认读取失败"),
    ADMIN_ROLLBACK_UNSUPPORTED("AD200006", "当前治理目标不支持完整回滚");

    public final String code;

    public final String message;
}
