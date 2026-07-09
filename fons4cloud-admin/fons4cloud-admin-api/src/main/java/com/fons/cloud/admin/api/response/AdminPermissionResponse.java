package com.fons.cloud.admin.api.response;

import com.fons.cloud.admin.api.enums.GovernanceAction;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * admin 可授权权限点。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminPermissionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限点编码。
     */
    private String permissionCode;

    /**
     * 权限所属治理域。
     */
    private GovernanceDomain domain;

    /**
     * 治理操作类型。
     */
    private GovernanceAction action;

    /**
     * 权限点说明。
     */
    private String description;
}
