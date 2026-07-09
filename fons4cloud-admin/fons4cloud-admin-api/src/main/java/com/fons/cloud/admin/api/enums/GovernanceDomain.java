package com.fons.cloud.admin.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 控制面按能力域组织治理入口，而不是按草稿、快照等状态对象组织顶层业务。
 */
@Getter
@AllArgsConstructor
public enum GovernanceDomain {

    ACCESS("access", "身份与权限治理"),
    CLIENTS("clients", "认证客户端治理"),
    SERVICES("services", "服务治理"),
    GATEWAY("gateway", "网关治理"),
    TRAFFIC("traffic", "流量治理"),
    OBSERVABILITY("observability", "可观测治理"),
    CHANGES("changes", "变更治理"),
    AUDITS("audits", "审计查询");

    private final String code;

    private final String description;
}
