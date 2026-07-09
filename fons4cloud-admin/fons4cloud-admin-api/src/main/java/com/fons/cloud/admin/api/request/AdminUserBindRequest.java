package com.fons.cloud.admin.api.request;

import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

/**
 * 绑定认证服务账号为 admin 管理员。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserBindRequest extends BaseRequest {

    /**
     * 认证服务账号 ID。
     */
    @NotNull(message = "认证账号ID不能为空")
    private Long accountId;

    /**
     * 认证账号用户名快照，用于展示和审计辅助。
     */
    private String username;

    /**
     * admin 展示名。
     */
    private String displayName;

    /**
     * 授予的 admin 角色编码。
     */
    @NotEmpty(message = "角色编码不能为空")
    private Set<String> roleCodes;

    /**
     * 绑定说明。
     */
    private String description;
}
