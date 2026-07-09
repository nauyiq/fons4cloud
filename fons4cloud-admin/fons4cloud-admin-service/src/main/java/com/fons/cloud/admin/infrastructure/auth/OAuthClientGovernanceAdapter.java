package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.response.GovernanceValidateResult;
import com.fons.cloud.admin.api.response.GovernanceValidationMessage;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientModifyRequest;
import com.fons.cloud.auth.request.OauthClientQueryRequest;
import com.fons.cloud.auth.request.OauthClientRotateSecretRequest;
import com.fons.cloud.auth.request.OauthClientStatusRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.common.result.R;
import com.fons.cloud.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * OAuth Client 治理适配器，通过认证服务 RPC 完成客户端新增、修改、启停和密钥轮换。
 */
@Component
public class OAuthClientGovernanceAdapter implements GovernanceTargetAdapter {

    private final AdminOauthClientManagementClient oauthClientManagementClient;

    /**
     * 创建 OAuth Client 治理适配器。
     *
     * @param oauthClientManagementClient admin 调用认证服务的 RPC 客户端端口
     */
    public OAuthClientGovernanceAdapter(AdminOauthClientManagementClient oauthClientManagementClient) {
        this.oauthClientManagementClient = oauthClientManagementClient;
    }

    @Override
    public GovernanceDomain domain() {
        return GovernanceDomain.CLIENTS;
    }

    @Override
    public CurrentConfig loadCurrent(ResourceRef resourceRef) {
        String clientId = resourceRef == null ? null : resourceRef.resourceKey();
        if (StringUtils.isBlank(clientId)) {
            String content = JsonUtil.toJson(new OauthClientInfo());
            return new CurrentConfig(content, hash(content), null);
        }
        R<OauthClientInfo> response = oauthClientManagementClient.query(OauthClientQueryRequest.builder()
                .clientId(clientId)
                .build());
        if (response == null || !response.isSuccess() || response.getData() == null) {
            String content = JsonUtil.toJson(new OauthClientInfo());
            return new CurrentConfig(content, hash(content), clientId);
        }
        String content = JsonUtil.toJson(response.getData());
        return new CurrentConfig(content, hash(content), clientId);
    }

    @Override
    public GovernanceValidateResult validate(TargetConfig targetConfig) {
        List<GovernanceValidationMessage> errors = new ArrayList<>();
        try {
            OAuthClientGovernanceCommand command = parseCommand(targetConfig.content());
            validateCommand(command, errors);
            String normalizedContent = JsonUtil.toJson(command.toSafeSnapshot());
            return GovernanceValidateResult.builder()
                    .passed(errors.isEmpty())
                    .errors(errors)
                    .warnings(List.of())
                    .normalizedContentHash(errors.isEmpty() ? hash(normalizedContent) : null)
                    .build();
        } catch (RuntimeException ex) {
            errors.add(message("content", "CLIENT_JSON_INVALID", "OAuth Client 治理配置必须是合法 JSON 对象"));
            return GovernanceValidateResult.builder()
                    .passed(false)
                    .errors(errors)
                    .warnings(List.of())
                    .build();
        }
    }

    @Override
    public AdapterPublishResult publish(TargetConfig targetConfig, PublishContext context) {
        OAuthClientGovernanceCommand command = parseCommand(targetConfig.content());
        ResourceRef resourceRef = targetConfig.resourceRef() == null
                ? new ResourceRef(domain(), "OAUTH_CLIENT", command.getClientId(), command.getClientId())
                : targetConfig.resourceRef();
        CurrentConfig before = loadCurrent(resourceRef);
        R<?> response = doPublish(command);
        if (response == null || !response.isSuccess()) {
            String errorMessage = response == null ? "认证服务 OAuth Client RPC 无响应" : response.getMessage();
            return new AdapterPublishResult(false, before.content(), before.contentHash(), null, null,
                    AdminResultCode.ADMIN_AUTH_RPC_FAILED.getCode(), errorMessage, null);
        }
        CurrentConfig confirmed = loadCurrent(new ResourceRef(domain(), "OAUTH_CLIENT", command.getClientId(), command.getClientId()));
        return new AdapterPublishResult(true, before.content(), before.contentHash(), confirmed.content(),
                confirmed.contentHash(), null, null, effectiveHint(command, response.getData()));
    }

    @Override
    public boolean rollbackSupported(ResourceRef resourceRef) {
        return false;
    }

    private R<?> doPublish(OAuthClientGovernanceCommand command) {
        OAuthClientOperation operation = OAuthClientOperation.valueOf(command.getOperation());
        return switch (operation) {
            case CREATE -> oauthClientManagementClient.create(toCreateRequest(command));
            case UPDATE -> oauthClientManagementClient.update(toModifyRequest(command));
            case STATUS -> oauthClientManagementClient.updateStatus(OauthClientStatusRequest.builder()
                    .clientId(command.getClientId())
                    .status(command.getStatus())
                    .version(command.getVersion())
                    .build());
            case ROTATE_SECRET -> oauthClientManagementClient.rotateSecret(OauthClientRotateSecretRequest.builder()
                    .clientId(command.getClientId())
                    .version(command.getVersion())
                    .build());
        };
    }

    private OauthClientCreateRequest toCreateRequest(OAuthClientGovernanceCommand command) {
        return OauthClientCreateRequest.builder()
                .clientId(command.getClientId())
                .resourceIds(command.getResourceIds())
                .scope(command.getScope())
                .authorizedGrantTypes(command.getAuthorizedGrantTypes())
                .webServerRedirectUri(command.getWebServerRedirectUri())
                .authorities(command.getAuthorities())
                .accessTokenValidity(command.getAccessTokenValidity())
                .refreshTokenValidity(command.getRefreshTokenValidity())
                .additionalInformation(command.getAdditionalInformation())
                .autoapprove(command.getAutoapprove())
                .status(command.getStatus())
                .build();
    }

    private OauthClientModifyRequest toModifyRequest(OAuthClientGovernanceCommand command) {
        return OauthClientModifyRequest.builder()
                .clientId(command.getClientId())
                .resourceIds(command.getResourceIds())
                .scope(command.getScope())
                .authorizedGrantTypes(command.getAuthorizedGrantTypes())
                .webServerRedirectUri(command.getWebServerRedirectUri())
                .authorities(command.getAuthorities())
                .accessTokenValidity(command.getAccessTokenValidity())
                .refreshTokenValidity(command.getRefreshTokenValidity())
                .additionalInformation(command.getAdditionalInformation())
                .autoapprove(command.getAutoapprove())
                .status(command.getStatus())
                .version(command.getVersion())
                .build();
    }

    private void validateCommand(OAuthClientGovernanceCommand command, List<GovernanceValidationMessage> errors) {
        if (StringUtils.isBlank(command.getOperation())) {
            errors.add(message("operation", "CLIENT_OPERATION_EMPTY", "OAuth Client 治理操作不能为空"));
        } else if (!validOperation(command.getOperation())) {
            errors.add(message("operation", "CLIENT_OPERATION_INVALID", "OAuth Client 治理操作不支持"));
        }
        if (StringUtils.isBlank(command.getClientId())) {
            errors.add(message("clientId", "CLIENT_ID_EMPTY", "OAuth Client ID 不能为空"));
        }
        if (StringUtils.isNotBlank(command.getClientSecret()) || StringUtils.isNotBlank(command.getNewClientSecret())) {
            errors.add(message("clientSecret", "CLIENT_SECRET_NOT_ALLOWED",
                    "admin 草稿不得保存 OAuth Client 明文密钥，创建和轮换由认证服务生成"));
        }
        if (OAuthClientOperation.STATUS.name().equals(command.getOperation()) && command.getStatus() == null) {
            errors.add(message("status", "CLIENT_STATUS_EMPTY", "启停 OAuth Client 时状态不能为空"));
        }
        if (StringUtils.isNotBlank(command.getAdditionalInformation())) {
            try {
                JsonUtil.jsonToMap(command.getAdditionalInformation());
            } catch (RuntimeException ex) {
                errors.add(message("additionalInformation", "CLIENT_ADDITIONAL_INFORMATION_INVALID",
                        "OAuth Client 扩展信息必须是合法 JSON 对象"));
            }
        }
    }

    private boolean validOperation(String operation) {
        try {
            OAuthClientOperation.valueOf(operation);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private OAuthClientGovernanceCommand parseCommand(String content) {
        if (StringUtils.isBlank(content)) {
            return new OAuthClientGovernanceCommand();
        }
        OAuthClientGovernanceCommand command = JsonUtil.toBean(content, OAuthClientGovernanceCommand.class);
        return command == null ? new OAuthClientGovernanceCommand() : command;
    }

    private String effectiveHint(OAuthClientGovernanceCommand command, Object data) {
        if (data instanceof OauthClientSecretRotateResult result) {
            return "OAuth Client " + command.getOperation() + " 已完成，密钥摘要：" + result.getMaskedClientSecret();
        }
        return "OAuth Client " + command.getOperation() + " 已完成";
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

    private enum OAuthClientOperation {
        CREATE,
        UPDATE,
        STATUS,
        ROTATE_SECRET
    }

    /**
     * OAuth Client 动作型治理命令。
     */
    @Getter
    @Setter
    public static class OAuthClientGovernanceCommand {

        /**
         * 治理操作：CREATE、UPDATE、STATUS、ROTATE_SECRET。
         */
        private String operation;

        /**
         * 客户端 ID。
         */
        private String clientId;

        /**
         * 明文密钥字段仅用于校验拦截，不能发布到认证服务或写入快照。
         */
        private String clientSecret;

        /**
         * 新明文密钥字段仅用于校验拦截，不能发布到认证服务或写入快照。
         */
        private String newClientSecret;

        /**
         * 客户端可访问的资源 ID 集合。
         */
        private String resourceIds;

        /**
         * 授权范围。
         */
        private String scope;

        /**
         * 授权模式集合。
         */
        private String authorizedGrantTypes;

        /**
         * 授权码模式回调地址。
         */
        private String webServerRedirectUri;

        /**
         * Spring Security 权限值集合。
         */
        private String authorities;

        /**
         * access_token 有效期，单位秒。
         */
        private Integer accessTokenValidity;

        /**
         * refresh_token 有效期，单位秒。
         */
        private Integer refreshTokenValidity;

        /**
         * 认证扩展信息，必须是 JSON 对象字符串。
         */
        private String additionalInformation;

        /**
         * 自动授权配置。
         */
        private String autoapprove;

        /**
         * 客户端启停状态。
         */
        private Boolean status;

        /**
         * 乐观锁版本号。
         */
        private Integer version;

        private OAuthClientGovernanceCommand toSafeSnapshot() {
            OAuthClientGovernanceCommand snapshot = new OAuthClientGovernanceCommand();
            snapshot.setOperation(operation);
            snapshot.setClientId(clientId);
            snapshot.setResourceIds(resourceIds);
            snapshot.setScope(scope);
            snapshot.setAuthorizedGrantTypes(authorizedGrantTypes);
            snapshot.setWebServerRedirectUri(webServerRedirectUri);
            snapshot.setAuthorities(authorities);
            snapshot.setAccessTokenValidity(accessTokenValidity);
            snapshot.setRefreshTokenValidity(refreshTokenValidity);
            snapshot.setAdditionalInformation(additionalInformation);
            snapshot.setAutoapprove(autoapprove);
            snapshot.setStatus(status);
            snapshot.setVersion(version);
            return snapshot;
        }
    }
}
