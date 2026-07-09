package com.fons.cloud.auth.api;

import com.alibaba.fastjson2.JSON;
import com.fons.cloud.auth.core.AuthorizationResourceRepository;
import com.fons.cloud.auth.utils.AuthUtils;
import com.fons.cloud.auth.utils.StaticEndpointAuthorizationManager;
import com.fons.cloud.util.AssertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 抽象权限校验模板类。
 *
 * @author qiyuan.hong
 * @version 1.0
 * @date 2022/10/10
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAuthPermissionService implements AuthPermissionService {

    private static final long BUSINESS_WHITE_URI_CACHE_MILLIS = 5000L;

    private final AuthorizationResourceRepository authorizationResourceRepository;
    private volatile List<String> businessWhiteUriCache = List.of();
    private volatile long businessWhiteUriCacheExpiredAt = 0L;

    @Override
    public final boolean isPermitAnonymousRequest(AuthenticationRequest request) {
        AssertUtil.notNull(request, "AuthenticationRequest should not be null.");
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.method())) {
            return true;
        }
        return isWhiteRequest(request);
    }

    @Override
    public final boolean isPermitRequest(AuthenticationRequest request) {
        AssertUtil.notNull(request, "AuthenticationRequest should not be null.");
        if (isPermitAnonymousRequest(request)) {
            return true;
        }

        List<String> authorities = request.authorities();
        if (CollectionUtils.isEmpty(authorities)) {
            log.warn("AuthenticationRequest has no authorities, request:{}", JSON.toJSONString(request));
            return false;
        }

        return authorizationResourceRepository.authenticate(request);
    }

    private boolean isWhiteRequest(AuthenticationRequest request) {
        String requestUri = request.requestUri();
        return isWhiteStaticEndpoint(requestUri)
                || isWhiteAccessIp(request.requestIp())
                || isBusinessWhiteAccessUri(requestUri);
    }

    @Override
    public boolean hasAuthorities(String... authorities) {
        List<String> currentAuthorities = AuthUtils.getCurrentAuthorities();
        return new HashSet<>(currentAuthorities).containsAll(List.of(authorities));
    }

    @Override
    public List<String> getBusinessWhiteUris() {
        long now = System.currentTimeMillis();
        if (now < businessWhiteUriCacheExpiredAt) {
            return businessWhiteUriCache;
        }
        synchronized (this) {
            if (now < businessWhiteUriCacheExpiredAt) {
                return businessWhiteUriCache;
            }
            businessWhiteUriCache = List.copyOf(new ArrayList<>(authorizationResourceRepository.getIgnoredAccessTokenUri()));
            businessWhiteUriCacheExpiredAt = now + BUSINESS_WHITE_URI_CACHE_MILLIS;
            return businessWhiteUriCache;
        }
    }

    protected boolean isWhiteStaticEndpoint(String requestUri) {
        if (StringUtils.isBlank(requestUri)) {
            return false;
        }
        return StaticEndpointAuthorizationManager.getInstance().isStaticWhiteEndpoint(requestUri);
    }

    /**
     * 判断当前请求 IP 是否命中白名单。
     *
     * @param requestIp 请求 IP
     * @return result.
     */
    protected abstract boolean isWhiteAccessIp(String requestIp);

    /**
     * 判断当前请求 URI 是否命中业务免 Token 白名单。
     *
     * @param requestUri request uri
     * @return result.
     */
    protected boolean isBusinessWhiteAccessUri(String requestUri) {
        try {
            List<String> whiteUris = this.getBusinessWhiteUris();
            return StaticEndpointAuthorizationManager.getInstance().isMatch(whiteUris, requestUri);
        } catch (Throwable cause) {
            log.error(cause.getMessage(), cause);
            return false;
        }
    }

}
