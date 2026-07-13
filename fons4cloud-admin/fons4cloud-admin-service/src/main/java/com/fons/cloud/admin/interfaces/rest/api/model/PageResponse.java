package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.List;

/** 有上限的统一分页响应。 */
public record PageResponse<T>(List<T> items, long total, int offset, int limit) {
    public PageResponse {
        items = List.copyOf(items);
    }
}
