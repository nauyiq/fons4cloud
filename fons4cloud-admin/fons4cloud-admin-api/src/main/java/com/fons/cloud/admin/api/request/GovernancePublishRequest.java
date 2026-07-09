package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 治理发布请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePublishRequest extends BaseRequest {

    /**
     * 待发布草稿ID。
     */
    @NotNull(message = "草稿ID不能为空")
    private Long draftId;

    /**
     * 用户确认发布时看到的目标基线 hash；与目标系统当前 hash 不一致时应拒绝发布。
     */
    private String expectedBaseHash;

    /**
     * 发布原因。
     */
    private String publishReason;
}
