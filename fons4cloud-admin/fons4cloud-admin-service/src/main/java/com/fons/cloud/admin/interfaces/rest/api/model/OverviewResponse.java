package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.List;
import java.util.Map;

/** 运维概览只返回可行动摘要，不返回配置正文。 */
public record OverviewResponse(
        List<ActionItem> actions,
        Map<String, Long> statistics,
        Map<String, AdminSessionContextResponse.DependencyState> dependencies) {

    public record ActionItem(String type, String severity, long count, String title, String route) {
    }
}
