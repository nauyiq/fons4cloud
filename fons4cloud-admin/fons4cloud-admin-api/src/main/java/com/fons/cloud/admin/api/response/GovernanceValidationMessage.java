package com.fons.cloud.admin.api.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 治理配置校验消息。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceValidationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发生问题的字段路径。
     */
    private String field;

    /**
     * 校验消息编码，便于前端国际化或规则定位。
     */
    private String code;

    /**
     * 脱敏后的校验消息。
     */
    private String message;
}
