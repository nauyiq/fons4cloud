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
 * admin 管理员角色关系实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user_role")
public class AdminUserRole extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 管理员 ID。
     */
    private Long adminUserId;

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 授权操作人。
     */
    private String grantedBy;

    /**
     * 授权时间。
     */
    private Date grantedAt;
}
