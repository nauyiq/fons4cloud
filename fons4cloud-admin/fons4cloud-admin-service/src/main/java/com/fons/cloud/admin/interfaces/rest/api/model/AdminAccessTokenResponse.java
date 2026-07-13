package com.fons.cloud.admin.interfaces.rest.api.model;

import java.util.Set;

/**
 * 浏览器可读取的短生命周期会话信息。
 *
 * <p>Refresh Token 必须只存在于 HttpOnly Cookie，因此该响应模型不包含刷新令牌字段。</p>
 */
public record AdminAccessTokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Set<String> scopes,
        Long userId,
        String username) {
}
