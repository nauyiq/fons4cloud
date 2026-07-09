package com.fons.cloud.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fons.cloud.db.entity.CommonEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.Date;

/**
 * admin 角色权限关系实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_role_permission")
public class AdminRolePermission extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 权限 ID。
     */
    private Long permissionId;

    /**
     * 授权操作人。
     */
    private String grantedBy;

    /**
     * 授权时间。
     */
    private Date grantedAt;
}
