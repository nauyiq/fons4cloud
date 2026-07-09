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
 * admin 治理变更草稿实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_governance_change")
public class AdminGovernanceChange extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理资源 ID。
     */
    private Long resourceId;

    /**
     * 变更单号，用于外部追踪和幂等查询。
     */
    private String changeNo;

    /**
     * 变更类型：CREATE 新增，UPDATE 修改，DELETE 删除。
     */
    private String changeType;

    /**
     * 变更状态，如 DRAFT、VALIDATED、PUBLISHING、PUBLISHED、FAILED、ROLLED_BACK。
     */
    private String status;

    /**
     * 创建草稿时读取到的目标摘要，用于发布前漂移检测。
     */
    private String baseHash;

    /**
     * 目标配置内容 JSON，允许保存受控配置正文。
     */
    private String content;

    /**
     * 目标配置内容摘要，用于审计和一致性校验。
     */
    private String contentHash;

    /**
     * 校验结果 JSON。
     */
    private String validationResult;

    /**
     * 草稿创建人。
     */
    private String createdBy;

    /**
     * 草稿最近更新人。
     */
    private String updatedBy;

    /**
     * 变更说明。
     */
    private String description;
}
