package com.fons.cloud.admin.domain.model;

import com.fons.cloud.admin.domain.entity.AdminGovernanceAudit;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * 治理审计领域模型，负责生成可落库的脱敏审计实体。
 */
@Getter
@Builder
public class GovernanceAudit {

    private static final int SUMMARY_MAX_LENGTH = 2048;
    private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"?(?:clientSecret|client_secret|accessToken|refreshToken|token|password|secret)\"?\\s*[:=]\\s*)\"?([^,}\\]\\s]+)\"?");

    /** 治理能力域。 */
    private final String domain;

    /** 治理资源 ID。 */
    private final Long resourceId;

    /** 治理变更 ID。 */
    private final Long changeId;

    /** 操作类型。 */
    private final String operation;

    /** 操作人 ID。 */
    private final String operatorId;

    /** 操作人展示名称快照。 */
    private final String operatorName;

    /** 请求追踪 ID。 */
    private final String requestId;

    /** 客户端 IP。 */
    private final String clientIp;

    /** 操作结果：SUCCESS、FAILED 或 DENIED。 */
    private final String result;

    /** 原始操作摘要，转换实体时会脱敏。 */
    private final String detailSummary;

    /** 错误码。 */
    private final String errorCode;

    /** 原始错误摘要，转换实体时会脱敏。 */
    private final String errorMessage;

    public AdminGovernanceAudit toEntity() {
        AdminGovernanceAudit audit = new AdminGovernanceAudit();
        audit.setDomain(domain);
        audit.setResourceId(resourceId);
        audit.setChangeId(changeId);
        audit.setOperation(operation);
        audit.setOperatorId(operatorId);
        audit.setOperatorName(operatorName);
        audit.setRequestId(requestId);
        audit.setClientIp(clientIp);
        audit.setResult(result);
        audit.setDetailSummary(mask(detailSummary));
        audit.setErrorCode(errorCode);
        audit.setErrorMessage(mask(errorMessage));
        audit.setOperatedAt(new Date());
        return audit;
    }

    /**
     * 对 token、clientSecret、password 等敏感字段做保守脱敏，并限制摘要长度。
     */
    public static String mask(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String masked = SENSITIVE_FIELD_PATTERN.matcher(text).replaceAll("$1***");
        return masked.length() > SUMMARY_MAX_LENGTH ? masked.substring(0, SUMMARY_MAX_LENGTH) : masked;
    }
}
