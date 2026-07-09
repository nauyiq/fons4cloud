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
 * admin 管理员绑定实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class AdminUser extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 认证服务账户 ID，admin 侧不保存来源客户端。
     */
    private Long accountId;

    /**
     * 认证账户用户名快照，用于列表展示和审计回显。
     */
    private String username;

    /**
     * 管理员展示名称。
     */
    private String displayName;

    /**
     * 管理员授权状态：ACTIVE 启用，DISABLED 禁用。
     */
    private String status;

    /**
     * 最近一次访问时间，仅用于展示和审计。
     */
    private Date lastAccessAt;

    /**
     * 最近一次访问 IP，仅用于展示和审计。
     */
    private String lastAccessIp;

    /**
     * 管理员绑定说明。
     */
    private String description;

    /**
     * 创建人账号或系统标识。
     */
    private String createdBy;

    /**
     * 最近更新人账号或系统标识。
     */
    private String updatedBy;
}
