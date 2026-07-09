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
 * admin 治理快照实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_governance_snapshot")
public class AdminGovernanceSnapshot extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理资源 ID。
     */
    private Long resourceId;

    /**
     * 关联变更 ID，当前态快照可为空。
     */
    private Long changeId;

    /**
     * 关联发布记录 ID，草稿快照可为空。
     */
    private Long releaseId;

    /**
     * 快照类型：BASE、BEFORE、AFTER、ROLLBACK_SOURCE。
     */
    private String snapshotType;

    /**
     * 快照配置内容 JSON，仅保存治理所需受控内容。
     */
    private String content;

    /**
     * 快照内容摘要。
     */
    private String contentHash;

    /**
     * 脱敏内容摘要，用于列表展示和审计。
     */
    private String contentSummary;

    /**
     * 建议保留截止时间，用于后续清理任务。
     */
    private Date retentionUntil;
}
