package com.fons.cloud.admin.api.request;

import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 创建或更新治理草稿。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceDraftCreateRequest extends BaseRequest {

    /**
     * 治理能力域，决定后续使用哪个治理适配器。
     */
    @NotNull(message = "治理能力域不能为空")
    private GovernanceDomain domain;

    /**
     * 资源类型；同一治理域内用于区分不同目标资源。
     */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /**
     * 资源唯一键；由治理适配器解释，不直接等同于数据库主键。
     */
    @NotBlank(message = "资源唯一键不能为空")
    private String resourceKey;

    /**
     * 用户开始编辑时看到的目标配置 hash，用于发布前漂移检测。
     */
    private String baseHash;

    /**
     * 期望配置内容 JSON。
     */
    @NotBlank(message = "治理配置内容不能为空")
    private String content;

    /**
     * 变更类型，默认由服务端按资源状态兜底为 UPDATE。
     */
    private GovernanceChangeType changeType;

    /**
     * 变更说明。
     */
    private String description;
}
