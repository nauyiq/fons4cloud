package com.fons.cloud.admin.infrastructure.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.api.response.GovernanceValidationMessage;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.util.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * 网关路由 Nacos 治理适配器，以 `gateway-routing.json` 为权威配置源。
 */
@Component
public class GatewayRouteGovernanceAdapter implements GovernanceTargetAdapter {

    private static final long NACOS_TIMEOUT_MS = 5000L;
    private static final String DEFAULT_DATA_ID = "gateway-routing.json";

    private final NacosConfigManager nacosConfigManager;
    private final String dataId;
    private final String group;

    /**
     * 创建网关路由治理适配器。
     *
     * @param nacosConfigProperties Nacos 配置属性
     * @param dataId                网关动态路由 dataId
     * @param group                 网关动态路由 group
     */
    @Autowired
    public GatewayRouteGovernanceAdapter(NacosConfigProperties nacosConfigProperties,
                                         @Value("${admin.gateway.route.data-id:gateway-routing.json}") String dataId,
                                         @Value("${admin.gateway.route.group:${NACOS_GROUP:DEFAULT_GROUP}}") String group) {
        this(new NacosConfigManager(nacosConfigProperties), dataId, group);
    }

    GatewayRouteGovernanceAdapter(NacosConfigManager nacosConfigManager, String dataId, String group) {
        this.nacosConfigManager = nacosConfigManager;
        this.dataId = StringUtils.defaultIfBlank(dataId, DEFAULT_DATA_ID);
        this.group = StringUtils.defaultIfBlank(group, "DEFAULT_GROUP");
    }

    @Override
    public GovernanceDomain domain() {
        return GovernanceDomain.GATEWAY;
    }

    @Override
    public CurrentConfig loadCurrent(ResourceRef resourceRef) {
        String targetDataId = targetDataId(resourceRef);
        try {
            String content = nacosConfigManager.getConfigService().getConfig(targetDataId, group, NACOS_TIMEOUT_MS);
            String normalized = normalize(content);
            return new CurrentConfig(normalized, hash(normalized), targetDataId);
        } catch (NacosException ex) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), ex.getErrMsg(), ex);
        }
    }

    @Override
    public GovernanceValidateResult validate(TargetConfig targetConfig) {
        List<GovernanceValidationMessage> errors = new ArrayList<>();
        String normalizedContent = null;
        try {
            List<Map> routes = parseRoutes(targetConfig.content());
            if (CollectionUtils.isEmpty(routes)) {
                errors.add(message("routes", "ROUTE_EMPTY", "网关路由配置不能为空"));
            } else {
                validateRoutes(routes, errors);
                normalizedContent = JsonUtil.toJson(routes);
            }
        } catch (RuntimeException ex) {
            errors.add(message("content", "ROUTE_JSON_INVALID", "网关路由配置必须是合法 JSON 数组"));
        }
        return GovernanceValidateResult.builder()
                .passed(errors.isEmpty())
                .errors(errors)
                .warnings(List.of())
                .normalizedContentHash(errors.isEmpty() ? hash(normalizedContent) : null)
                .build();
    }

    @Override
    public AdapterPublishResult publish(TargetConfig targetConfig, PublishContext context) {
        ResourceRef resourceRef = targetConfig.resourceRef();
        CurrentConfig before = loadCurrent(resourceRef);
        String afterContent = normalize(targetConfig.content());
        String afterHash = StringUtils.defaultIfBlank(targetConfig.contentHash(), hash(afterContent));
        try {
            boolean published = nacosConfigManager.getConfigService()
                    .publishConfig(targetDataId(resourceRef), group, afterContent);
            if (!published) {
                return new AdapterPublishResult(false, before.content(), before.contentHash(), null, null,
                        AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), "Nacos 配置发布返回失败", null);
            }
            return new AdapterPublishResult(true, before.content(), before.contentHash(), afterContent, afterHash,
                    null, null, "网关监听 Nacos 配置刷新");
        } catch (NacosException ex) {
            return new AdapterPublishResult(false, before.content(), before.contentHash(), null, null,
                    AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), ex.getErrMsg(), null);
        }
    }

    @Override
    public boolean rollbackSupported(ResourceRef resourceRef) {
        return true;
    }

    /**
     * 网关动态路由以单个 Nacos dataId 保存完整路由数组；resourceKey 仅用于定位数组中的路由，
     * 不能被当作 dataId 使用。
     */
    private String targetDataId(ResourceRef resourceRef) {
        return dataId;
    }

    private List<Map> parseRoutes(String content) {
        if (StringUtils.isBlank(content)) {
            return List.of();
        }
        return JsonUtil.toList(content, Map.class);
    }

    private void validateRoutes(List<Map> routes, List<GovernanceValidationMessage> errors) {
        for (int index = 0; index < routes.size(); index++) {
            Map route = routes.get(index);
            if (StringUtils.isBlank(text(route, "id"))) {
                errors.add(message("[" + index + "].id", "ROUTE_ID_EMPTY", "路由 ID 不能为空"));
            }
            if (StringUtils.isBlank(text(route, "uri"))) {
                errors.add(message("[" + index + "].uri", "ROUTE_URI_EMPTY", "路由 URI 不能为空"));
            }
            Object predicates = route.get("predicates");
            if (!(predicates instanceof List<?> predicateList) || predicateList.isEmpty()) {
                errors.add(message("[" + index + "].predicates", "ROUTE_PREDICATES_EMPTY",
                        "路由 predicates 不能为空"));
            }
        }
    }

    private String text(Map route, String key) {
        Object value = route.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private GovernanceValidationMessage message(String field, String code, String message) {
        return GovernanceValidationMessage.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }

    private String normalize(String content) {
        if (StringUtils.isBlank(content)) {
            return "[]";
        }
        return JsonUtil.toJson(parseRoutes(content));
    }

    private String hash(String content) {
        if (content == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
