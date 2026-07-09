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
 * admin 治理审计实体。
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_governance_audit")
public class AdminGovernanceAudit extends CommonEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理域。
     */
    private String domain;

    /**
     * 治理资源 ID。
     */
    private Long resourceId;

    /**
     * 治理变更 ID。
     */
    private Long changeId;

    /**
     * 操作类型，如 LOGIN、DRAFT_CREATE、VALIDATE、PUBLISH、ROLLBACK、ROLE_GRANT。
     */
    private String operation;

    /**
     * 操作人 ID。
     */
    private String operatorId;

    /**
     * 操作人展示名称快照。
     */
    private String operatorName;

    /**
     * 请求追踪 ID。
     */
    private String requestId;

    /**
     * 客户端 IP。
     */
    private String clientIp;

    /**
     * 操作结果：SUCCESS 成功，FAILED 失败，DENIED 拒绝。
     */
    private String result;

    /**
     * 脱敏操作摘要，不得写入 token、clientSecret 或完整配置正文。
     */
    private String detailSummary;

    /**
     * 错误码。
     */
    private String errorCode;

    /**
     * 脱敏错误摘要。
     */
    private String errorMessage;

    /**
     * 操作发生时间。
     */
    private Date operatedAt;
}
