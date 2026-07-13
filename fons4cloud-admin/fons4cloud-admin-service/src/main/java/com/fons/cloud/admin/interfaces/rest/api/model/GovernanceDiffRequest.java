package com.fons.cloud.admin.interfaces.rest.api.model;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 语义差异请求，配置正文只用于当前请求，不写日志。 */
public record GovernanceDiffRequest(
        @NotNull GovernanceDomain domain,
        @NotBlank String resourceType,
        @NotBlank String beforeContent,
        @NotBlank String afterContent) {
}
