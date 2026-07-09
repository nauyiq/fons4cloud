package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 基于历史快照发起回滚。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceRollbackRequest extends BaseRequest {

    /**
     * 用作回滚来源的历史快照ID。
     */
    @NotNull(message = "快照ID不能为空")
    private Long snapshotId;

    /**
     * 回滚前调用方确认的当前目标 hash；与目标系统当前 hash 不一致时应拒绝回滚。
     */
    private String expectedCurrentHash;

    /**
     * 回滚原因。
     */
    private String rollbackReason;
}
