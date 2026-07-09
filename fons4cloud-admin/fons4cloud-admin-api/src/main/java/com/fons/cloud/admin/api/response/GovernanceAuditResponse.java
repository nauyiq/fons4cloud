package com.fons.cloud.admin.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * admin 审计查询响应，所有详情字段均为脱敏摘要。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAuditResponse {

    /** 审计记录 ID。 */
    private Long id;

    /** 治理能力域。 */
    private String domain;

    /** 治理资源 ID。 */
    private Long resourceId;

    /** 治理变更 ID。 */
    private Long changeId;

    /** 操作类型。 */
    private String operation;

    /** 操作人 ID。 */
    private String operatorId;

    /** 操作人展示名称快照。 */
    private String operatorName;

    /** 请求追踪 ID。 */
    private String requestId;

    /** 客户端 IP。 */
    private String clientIp;

    /** 操作结果。 */
    private String result;

    /** 脱敏操作摘要。 */
    private String detailSummary;

    /** 错误码。 */
    private String errorCode;

    /** 脱敏错误摘要。 */
    private String errorMessage;

    /** 操作发生时间。 */
    private Date operatedAt;
}
