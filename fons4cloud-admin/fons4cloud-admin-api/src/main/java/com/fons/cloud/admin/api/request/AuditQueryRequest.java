package com.fons.cloud.admin.api.request;

import com.fons.cloud.admin.api.enums.GovernanceAuditResult;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.common.request.BaseRequest;
import lombok.*;

import java.util.Date;

/**
 * admin 审计查询条件。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditQueryRequest extends BaseRequest {

    /**
     * 治理能力域过滤条件。
     */
    private GovernanceDomain domain;

    /**
     * 资源唯一键过滤条件。
     */
    private String resourceKey;

    /**
     * 操作类型过滤条件。
     */
    private String operation;

    /**
     * 操作人标识。
     */
    private String operatorId;

    /**
     * 操作结果过滤条件。
     */
    private GovernanceAuditResult result;

    /**
     * 审计发生时间起点。
     */
    private Date startTime;

    /**
     * 审计发生时间终点。
     */
    private Date endTime;
}
