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
 * admin 治理资源登记实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_governance_resource")
public class AdminGovernanceResource extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理域。
     */
    private String domain;

    /**
     * 资源类型，如 ROUTE、IP_WHITE_LIST、OAUTH_CLIENT。
     */
    private String resourceType;

    /**
     * 资源唯一键，由治理适配器按目标系统规则生成。
     */
    private String resourceKey;

    /**
     * 权威目标引用，如 Nacos dataId、Redis key 或认证客户端 ID。
     */
    private String targetRef;

    /**
     * 最近确认的目标内容摘要，用于漂移检测。
     */
    private String currentHash;

    /**
     * 最近一次当前态快照 ID。
     */
    private Long currentSnapshotId;

    /**
     * 治理资源状态：ACTIVE 可治理，DISABLED 停用。
     */
    private String status;

    /**
     * 资源说明。
     */
    private String description;
}
