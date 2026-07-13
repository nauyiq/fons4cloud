package com.fons.cloud.admin.interfaces.rest.api.model;

import jakarta.validation.constraints.NotBlank;

/** 可编辑草稿更新请求。 */
public record GovernanceDraftUpdateRequest(@NotBlank String content, String description) {
}
