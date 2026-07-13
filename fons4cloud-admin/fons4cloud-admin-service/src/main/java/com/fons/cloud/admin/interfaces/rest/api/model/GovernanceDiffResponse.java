package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.List;

/** 服务端语义差异响应；敏感字段不返回原值。 */
public record GovernanceDiffResponse(boolean changed, List<DiffEntry> entries) {

    public record DiffEntry(String path, Operation operation, Object before, Object after, boolean sensitive) {
    }

    public enum Operation {
        ADD,
        REMOVE,
        REPLACE
    }
}
