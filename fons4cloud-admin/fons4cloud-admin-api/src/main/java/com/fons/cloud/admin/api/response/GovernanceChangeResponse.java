package com.fons.cloud.admin.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 治理变更响应。用于草稿、校验、发布和回滚接口返回受控变更状态。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceChangeResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 治理变更 ID。
     */
    private Long id;

    /**
     * 治理资源 ID。
     */
    private Long resourceId;

    /**
     * 变更单号，用于外部追踪和幂等查询。
     */
    private String changeNo;

    /**
     * 变更类型，如 CREATE、UPDATE、DELETE、ROLLBACK。
     */
    private String changeType;

    /**
     * 变更生命周期状态。
     */
    private String status;

    /**
     * 创建草稿时读取到的目标配置基线 hash。
     */
    private String baseHash;

    /**
     * 期望发布内容的 hash。
     */
    private String contentHash;

    /**
     * 目标配置正文。列表场景后续可通过专门摘要模型收敛。
     */
    private String content;

    /**
     * 校验结果 JSON。
     */
    private String validationResult;

    /**
     * 变更说明。
     */
    private String description;

    /**
     * 草稿创建人。
     */
    private String createdBy;

    /**
     * 最近更新人。
     */
    private String updatedBy;
}
