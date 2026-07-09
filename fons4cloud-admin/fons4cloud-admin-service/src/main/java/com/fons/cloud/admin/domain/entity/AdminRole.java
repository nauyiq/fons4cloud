package com.fons.cloud.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fons.cloud.db.entity.CommonEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * admin 角色实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_role")
public class AdminRole extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色编码，作为授权判断和初始化的稳定标识。
     */
    private String roleCode;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色类型：BUILT_IN 内置，CUSTOM 自定义。
     */
    private String roleType;

    /**
     * 角色状态：ACTIVE 启用，DISABLED 禁用。
     */
    private String status;

    /**
     * 角色说明。
     */
    private String description;
}
