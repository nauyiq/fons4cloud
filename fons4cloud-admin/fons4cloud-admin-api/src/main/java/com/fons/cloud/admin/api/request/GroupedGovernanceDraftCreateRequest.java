package com.fons.cloud.admin.api.request;

import com.fons.cloud.admin.api.enums.GovernanceChangeType;
import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 分组治理 API 创建草稿请求。
 *
 * <p>分组接口的治理域和资源类型由 REST 路径决定，调用方只提交资源键和目标配置。</p>
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupedGovernanceDraftCreateRequest extends BaseRequest {

    /**
     * 资源唯一键；由具体治理适配器解释。
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
