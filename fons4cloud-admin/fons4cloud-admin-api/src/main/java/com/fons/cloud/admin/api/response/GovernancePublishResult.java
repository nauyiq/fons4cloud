package com.fons.cloud.admin.api.response;

import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 治理发布结果。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePublishResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发布记录ID。
     */
    private Long releaseId;

    /**
     * 发布前目标系统内容摘要，用于漂移和回滚追踪。
     */
    private String beforeHash;

    /**
     * 发布后目标系统内容摘要。
     */
    private String afterHash;

    /**
     * 发布结果状态。
     */
    private GovernanceReleaseStatus status;

    /**
     * 目标运行时感知方式提示，如网关监听 Nacos 刷新。
     */
    private String effectiveHint;
}
