package com.fons.cloud.admin.api.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 治理配置校验结果。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceValidateResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否通过校验。
     */
    private Boolean passed;

    /**
     * 阻断发布的错误列表。
     */
    private List<GovernanceValidationMessage> errors;

    /**
     * 不阻断发布但需要提示的警告列表。
     */
    private List<GovernanceValidationMessage> warnings;

    /**
     * 标准化后的内容摘要，用于后续发布前一致性判断。
     */
    private String normalizedContentHash;
}
