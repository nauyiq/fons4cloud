package com.fons.cloud.auth.oauth.core;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.lang.Assert;

import com.fons.cloud.cache.redis.server.support.RedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;

import java.util.concurrent.TimeUnit;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/2/27 11:25
 */
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {
    private final static Long TIMEOUT = 10L;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, () -> new RuntimeException("authorizationConsent cannot be null"));
        RedisManager.getInstance().set(buildKey(authorizationConsent), authorizationConsent, TIMEOUT, TimeUnit.MINUTES);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, () -> new RuntimeException("authorizationConsent cannot be null"));
        RedisManager.getInstance().del(buildKey(authorizationConsent));
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        Assert.isTrue(StringUtils.isNotBlank(registeredClientId), () -> new RuntimeException("registeredClientId cannot be empty"));
        Assert.isTrue(StringUtils.isNotBlank(principalName), () -> new RuntimeException("principalName cannot be empty"));
        return RedisManager.getInstance().get(buildKey(registeredClientId, principalName));
    }

    private static String buildKey(OAuth2AuthorizationConsent authorizationConsent) {
        return buildKey(authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
    }

    private static String buildKey(String registeredClientId, String principalName) {
        return "token:consent:" + registeredClientId + ":" + principalName;
    }

}
