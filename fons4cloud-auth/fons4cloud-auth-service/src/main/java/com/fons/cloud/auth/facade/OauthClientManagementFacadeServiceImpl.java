package com.fons.cloud.auth.facade;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.auth.constants.OauthClientManageResultCode;
import com.fons.cloud.auth.domain.entity.OauthClient;
import com.fons.cloud.auth.domain.service.SysOauthClientDomainService;
import com.fons.cloud.auth.infrastructure.converter.OauthClientManagementConverter;
import com.fons.cloud.auth.request.*;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.auth.service.OauthClientManagementFacadeService;
import com.fons.cloud.common.result.R;
import com.fons.cloud.dubbo.DubboConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * OAuth Client 管理 RPC Dubbo 实现。
 */
@Slf4j
@RequiredArgsConstructor
@DubboService(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)
public class OauthClientManagementFacadeServiceImpl implements OauthClientManagementFacadeService {

    private static final int GENERATED_SECRET_LENGTH = 32;

    private static final OauthClientManagementConverter CONVERTER = OauthClientManagementConverter.CONVERTER;

    private final PasswordEncoder passwordEncoder;

    private final SysOauthClientDomainService oauthClientDomainService;

    @Override
    public R<OauthClientInfo> query(OauthClientQueryRequest request) {
        OauthClient client = oauthClientDomainService.findByClientId(request.getClientId());
        if (client == null) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_NOT_FOUND);
        }
        return R.ok(CONVERTER.mapToInfo(client));
    }

    @Override
    public R<List<OauthClientInfo>> queryList(OauthClientQueryRequest request) {
        LambdaQueryWrapper<OauthClient> wrapper = new LambdaQueryWrapper<>();
        if (request != null) {
            wrapper.eq(StringUtils.isNotBlank(request.getClientId()), OauthClient::getClientId, request.getClientId());
            wrapper.eq(request.getStatus() != null, OauthClient::getStatus, request.getStatus());
        }
        return R.ok(oauthClientDomainService.list(wrapper).stream().map(CONVERTER::mapToInfo).toList());
    }

    @Override
    public R<OauthClientSecretRotateResult> create(OauthClientCreateRequest request) {
        OauthClient exist = oauthClientDomainService.getById(request.getClientId());
        if (exist != null) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_ALREADY_EXISTS);
        }

        String plainSecret = normalizeSecret(request.getClientSecret());
        OauthClient client = CONVERTER.createEntity(request);
        client.setClientSecret(passwordEncoder.encode(plainSecret));
        client.setScope(StringUtils.defaultIfBlank(request.getScope(), "all"));
        if (request.getStatus() == null) {
            client.setStatus(Boolean.TRUE);
        }

        if (!oauthClientDomainService.save(client)) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_SAVE_FAILED);
        }
        oauthClientDomainService.evictClientCache(client.getClientId());
        return R.ok(secretResult(client.getClientId(), plainSecret, client.getVersion()));
    }

    @Override
    public R<OauthClientInfo> update(OauthClientModifyRequest request) {
        OauthClient client = oauthClientDomainService.getById(request.getClientId());
        if (client == null) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_NOT_FOUND);
        }

        CONVERTER.updateEntity(request, client);

        if (!oauthClientDomainService.updateById(client)) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_UPDATE_FAILED);
        }
        oauthClientDomainService.evictClientCache(client.getClientId());
        return R.ok(CONVERTER.mapToInfo(oauthClientDomainService.getById(client.getClientId())));
    }

    @Override
    public R<Boolean> updateStatus(OauthClientStatusRequest request) {
        OauthClient client = oauthClientDomainService.getById(request.getClientId());
        if (client == null) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_NOT_FOUND);
        }
        client.setStatus(request.getStatus());
        client.setVersion(request.getVersion());
        boolean updated = oauthClientDomainService.updateById(client);
        if (updated) {
            oauthClientDomainService.evictClientCache(client.getClientId());
            return R.ok(Boolean.TRUE);
        }
        return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_UPDATE_FAILED);
    }

    @Override
    public R<OauthClientSecretRotateResult> rotateSecret(OauthClientRotateSecretRequest request) {
        OauthClient client = oauthClientDomainService.getById(request.getClientId());
        if (client == null) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_NOT_FOUND);
        }

        String plainSecret = normalizeSecret(request.getNewClientSecret());
        client.setClientSecret(passwordEncoder.encode(plainSecret));
        client.setVersion(request.getVersion());
        if (!oauthClientDomainService.updateById(client)) {
            return R.failed(OauthClientManageResultCode.OAUTH_CLIENT_SECRET_ROTATE_FAILED);
        }
        oauthClientDomainService.evictClientCache(client.getClientId());
        return R.ok(secretResult(client.getClientId(), plainSecret, client.getVersion()));
    }

    @Override
    public R<Boolean> evictCache(String clientId) {
        oauthClientDomainService.evictClientCache(clientId);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 统一处理密钥输入：调用方未传明文密钥时，由认证服务生成随机密钥。
     */
    private String normalizeSecret(String secret) {
        return StringUtils.isBlank(secret) ? RandomUtil.randomString(GENERATED_SECRET_LENGTH) : secret;
    }

    /**
     * 构造密钥创建或轮换响应；明文只通过该一次性结果返回，不写入日志和普通视图。
     */
    private OauthClientSecretRotateResult secretResult(String clientId, String plainSecret, Integer version) {
        return OauthClientSecretRotateResult.builder()
                .clientId(clientId)
                .plainClientSecret(plainSecret)
                .maskedClientSecret(maskSecret(plainSecret))
                .version(version)
                .build();
    }

    /**
     * 明文密钥脱敏展示规则：短密钥完全隐藏，长密钥仅展示首尾各四位。
     */
    private String maskSecret(String plainSecret) {
        if (StringUtils.length(plainSecret) <= 8) {
            return "******";
        }
        return StringUtils.left(plainSecret, 4) + "******" + StringUtils.right(plainSecret, 4);
    }
}
