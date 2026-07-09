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
 * admin 权限点目录实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_permission")
public class AdminPermission extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限编码，由 admin API 权限常量约束。
     */
    private String permissionCode;

    /**
     * 治理域，用于按框架能力边界组织权限。
     */
    private String domain;

    /**
     * 操作类型，如 READ、DRAFT、VALIDATE、PUBLISH、ROLLBACK、MANAGE。
     */
    private String action;

    /**
     * 权限说明。
     */
    private String description;

    /**
     * 权限状态：ACTIVE 启用，DISABLED 禁用。
     */
    private String status;
}
