package com.fons.cloud.admin.api.request;

import com.fons.cloud.admin.api.enums.AdminUserStatus;
import com.fons.cloud.common.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

/**
 * admin 角色保存请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminRoleSaveRequest extends BaseRequest {

    /**
     * 角色编码；作为授权绑定和初始化的稳定标识。
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称，用于页面展示。
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色绑定的权限点编码。
     */
    private Set<String> permissionCodes;

    /**
     * 角色启停状态。
     */
    private AdminUserStatus status;

    /**
     * 角色说明。
     */
    private String description;
}
