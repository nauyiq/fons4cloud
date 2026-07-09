package com.fons.cloud.admin.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * admin 权限动作类型。
 */
@Getter
@AllArgsConstructor
public enum GovernanceAction {

    VIEW("view", "查看"),
    EDIT("edit", "编辑草稿"),
    PUBLISH("publish", "发布"),
    ROLLBACK("rollback", "回滚");

    private final String code;

    private final String description;
}
