package com.fons.cloud.admin.infrastructure.security;

import com.fons.cloud.common.result.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * admin 运行期鉴权结果，封装是否放行以及拒绝时返回的业务错误码。
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminAuthorizationDecision {

    /**
     * 当前请求是否允许继续访问 admin API。
     */
    private final boolean allowed;

    /**
     * 拒绝访问时返回给调用方的错误码；放行时为空。
     */
    private final Result deniedResult;

    public static AdminAuthorizationDecision allow() {
        return new AdminAuthorizationDecision(true, null);
    }

    public static AdminAuthorizationDecision deny(Result deniedResult) {
        return new AdminAuthorizationDecision(false, deniedResult);
    }
}
