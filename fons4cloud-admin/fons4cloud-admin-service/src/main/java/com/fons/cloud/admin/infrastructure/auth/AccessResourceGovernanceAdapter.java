package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.api.response.GovernanceValidationMessage;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.auth.core.AuthorizationResourceRepository;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 授权资源治理适配器，复用 auth-core 的全局授权资源、忽略 Token URI 和幂等 URI 仓储。
 */
@Component
public class AccessResourceGovernanceAdapter implements GovernanceTargetAdapter {

    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("^[A-Z]+_/.+");
    private static final Set<String> SUPPORTED_HTTP_METHODS = Set.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.HEAD.name()
    );

    private final AuthorizationResourceRepository repository;

    /**
     * 创建授权资源治理适配器。
     *
     * @param repository auth-core 全局授权资源仓储
     */
    public AccessResourceGovernanceAdapter(AuthorizationResourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public GovernanceDomain domain() {
        return GovernanceDomain.ACCESS;
    }

    @Override
    public CurrentConfig loadCurrent(ResourceRef resourceRef) {
        try {
            AccessResourceConfig config = new AccessResourceConfig();
            config.setAuthorizationResources(toResourceItems(repository.getAuthorizationResources()));
            config.setIgnoredAccessTokenUris(new ArrayList<>(repository.getIgnoredAccessTokenUri()));
            config.setIdentifierTokenUris(new ArrayList<>(repository.getIdentifierTokenUri()));
            String content = normalize(config);
            return new CurrentConfig(content, hash(content), "auth:authorization-resource");
        } catch (RuntimeException ex) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), "读取授权资源配置失败", ex);
        }
    }

    @Override
    public GovernanceValidateResult validate(TargetConfig targetConfig) {
        List<GovernanceValidationMessage> errors = new ArrayList<>();
        String normalizedContent = null;
        try {
            AccessResourceConfig config = parseConfig(targetConfig.content());
            validateResources(config.getAuthorizationResources(), errors);
            validateUris("ignoredAccessTokenUris", config.getIgnoredAccessTokenUris(), errors);
            validateUris("identifierTokenUris", config.getIdentifierTokenUris(), errors);
            normalizedContent = normalize(config);
        } catch (RuntimeException ex) {
            errors.add(message("content", "ACCESS_JSON_INVALID", "授权资源治理配置必须是合法 JSON 对象"));
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
        CurrentConfig before = loadCurrent(targetConfig.resourceRef());
        AccessResourceConfig afterConfig = parseConfig(targetConfig.content());
        try {
            repository.replaceAuthorizationResources(toResourceMap(afterConfig.getAuthorizationResources()));
            repository.replaceIgnoredAccessTokenUri(normalizeUriSet(afterConfig.getIgnoredAccessTokenUris()));
            repository.replaceIdentifierTokenUri(normalizeUriSet(afterConfig.getIdentifierTokenUris()));
            CurrentConfig confirmed = loadCurrent(targetConfig.resourceRef());
            return new AdapterPublishResult(true, before.content(), before.contentHash(), confirmed.content(),
                    confirmed.contentHash(), null, null, "auth-core 授权资源仓储已刷新");
        } catch (RuntimeException ex) {
            return new AdapterPublishResult(false, before.content(), before.contentHash(), null, null,
                    AdminResultCode.ADMIN_TARGET_UNAVAILABLE.getCode(), ex.getMessage(), null);
        }
    }

    @Override
    public boolean rollbackSupported(ResourceRef resourceRef) {
        return true;
    }

    private void validateResources(List<AuthorizationResourceItem> resources, List<GovernanceValidationMessage> errors) {
        if (CollectionUtils.isEmpty(resources)) {
            errors.add(message("authorizationResources", "ACCESS_RESOURCE_EMPTY", "授权资源集合不能为空"));
            return;
        }
        Set<String> exists = new TreeSet<>();
        for (int index = 0; index < resources.size(); index++) {
            AuthorizationResourceItem resource = resources.get(index);
            String id = resource == null ? null : trim(resource.getId());
            if (!validResourceId(id)) {
                errors.add(message("authorizationResources[" + index + "].id", "ACCESS_RESOURCE_ID_INVALID",
                        "授权资源 ID 必须使用 METHOD_/path 格式"));
            } else if (!exists.add(id)) {
                errors.add(message("authorizationResources[" + index + "].id", "ACCESS_RESOURCE_DUPLICATED",
                        "授权资源 ID 不能重复"));
            }
            if (resource == null || CollectionUtils.isEmpty(resource.getAuthorities())) {
                errors.add(message("authorizationResources[" + index + "].authorities", "ACCESS_AUTHORITIES_EMPTY",
                        "授权资源权限集合不能为空"));
            } else if (resource.getAuthorities().stream().anyMatch(StringUtils::isBlank)) {
                errors.add(message("authorizationResources[" + index + "].authorities", "ACCESS_AUTHORITIES_BLANK",
                        "授权资源权限值不能为空"));
            }
        }
    }

    private void validateUris(String field, List<String> uris, List<GovernanceValidationMessage> errors) {
        if (CollectionUtils.isEmpty(uris)) {
            return;
        }
        Set<String> exists = new TreeSet<>();
        for (int index = 0; index < uris.size(); index++) {
            String uri = trim(uris.get(index));
            if (!validUri(uri)) {
                errors.add(message(field + "[" + index + "]", "ACCESS_URI_INVALID", "URI 必须以 / 开头且不能包含空白字符"));
            } else if (!exists.add(uri)) {
                errors.add(message(field + "[" + index + "]", "ACCESS_URI_DUPLICATED", "URI 不能重复"));
            }
        }
    }

    private List<AuthorizationResourceItem> toResourceItems(Map<String, Set<String>> resourceMap) {
        if (resourceMap == null || resourceMap.isEmpty()) {
            return List.of();
        }
        return resourceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AuthorizationResourceItem(entry.getKey(), new ArrayList<>(
                        normalizeAuthoritySet(entry.getValue() == null ? List.of() : new ArrayList<>(entry.getValue())))))
                .toList();
    }

    private Map<String, Set<String>> toResourceMap(List<AuthorizationResourceItem> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return new TreeMap<>();
        }
        return resources.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getId()))
                .sorted(Comparator.comparing(AuthorizationResourceItem::getId))
                .collect(Collectors.toMap(item -> trim(item.getId()), item -> normalizeAuthoritySet(item.getAuthorities()),
                        (left, right) -> right, TreeMap::new));
    }

    private Set<String> normalizeAuthoritySet(List<String> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return new LinkedHashSet<>();
        }
        return authorities.stream()
                .map(this::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Set<String> normalizeUriSet(List<String> uris) {
        if (CollectionUtils.isEmpty(uris)) {
            return new TreeSet<>();
        }
        return uris.stream()
                .map(this::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private AccessResourceConfig parseConfig(String content) {
        if (StringUtils.isBlank(content)) {
            return new AccessResourceConfig();
        }
        AccessResourceConfig config = JsonUtil.toBean(content, AccessResourceConfig.class);
        return config == null ? new AccessResourceConfig() : config;
    }

    private String normalize(AccessResourceConfig config) {
        AccessResourceConfig normalized = new AccessResourceConfig();
        normalized.setAuthorizationResources(new ArrayList<>(toResourceMap(config.getAuthorizationResources()).entrySet()
                .stream()
                .map(entry -> new AuthorizationResourceItem(entry.getKey(), new ArrayList<>(entry.getValue())))
                .toList()));
        normalized.setIgnoredAccessTokenUris(new ArrayList<>(normalizeUriSet(config.getIgnoredAccessTokenUris())));
        normalized.setIdentifierTokenUris(new ArrayList<>(normalizeUriSet(config.getIdentifierTokenUris())));
        return JsonUtil.toJson(normalized);
    }

    private boolean validResourceId(String id) {
        if (StringUtils.isBlank(id) || !RESOURCE_ID_PATTERN.matcher(id).matches()) {
            return false;
        }
        String method = id.substring(0, id.indexOf('_'));
        String uri = id.substring(id.indexOf('_') + 1);
        return SUPPORTED_HTTP_METHODS.contains(method) && validUri(uri);
    }

    private boolean validUri(String uri) {
        return StringUtils.isNotBlank(uri) && uri.startsWith("/") && !containsWhitespace(uri);
    }

    private boolean containsWhitespace(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isWhitespace(value.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private GovernanceValidationMessage message(String field, String code, String message) {
        return GovernanceValidationMessage.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
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

    /**
     * 授权资源治理配置。
     */
    @Getter
    @Setter
    public static class AccessResourceConfig {

        /**
         * METHOD_URI 授权资源及其所需权限点。
         */
        private List<AuthorizationResourceItem> authorizationResources = List.of();

        /**
         * 忽略 Access Token 校验的业务 URI。
         */
        private List<String> ignoredAccessTokenUris = List.of();

        /**
         * 需要幂等标识 Token 校验的业务 URI。
         */
        private List<String> identifierTokenUris = List.of();
    }

    /**
     * 单个 METHOD_URI 授权资源。
     */
    @Getter
    @Setter
    public static class AuthorizationResourceItem {

        /**
         * 资源 ID，格式为 HTTP 方法名 + 下划线 + URI，例如 `GET_/admin/audits`。
         */
        private String id;

        /**
         * 访问该资源需要具备的权限点集合。
         */
        private List<String> authorities = List.of();

        public AuthorizationResourceItem() {
        }

        public AuthorizationResourceItem(String id, List<String> authorities) {
            this.id = id;
            this.authorities = authorities;
        }
    }
}
