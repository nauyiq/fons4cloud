package com.fons.cloud.admin.api.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * admin 登录成功后的 Token 响应。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminTokenResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * admin 访问令牌；由 admin-service 通过 auth-service RPC 获取，不在日志中输出。
     */
    @ToString.Exclude
    private String accessToken;

    /**
     * admin 刷新令牌；不在日志中输出。
     */
    @ToString.Exclude
    private String refreshToken;

    /**
     * Token 类型，通常为 Bearer。
     */
    private String tokenType;

    /**
     * accessToken 剩余有效期，单位秒。
     */
    private Long expiresIn;

    /**
     * 授权范围集合。
     */
    private Set<String> scopes;

    /**
     * admin 自有用户ID，不等同于认证服务账户ID。
     */
    private Long userId;

    /**
     * 登录管理员用户名快照。
     */
    private String username;
}
