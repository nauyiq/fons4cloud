package com.fons.cloud.auth.satoken.api;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

import java.util.List;

/**
 * Sa-Token 认证工具封装，业务方注入即用。
 * <p>
 * 仅承载登录、登出、会话校验、令牌查询与踢人下线等通用能力；
 * 权限/角色数据由业务方实现 {@link cn.dev33.satoken.stp.StpInterface} 提供。
 * <p>
 * 令牌同时支持 Header（请求头名 = {@code sa-token.token-name}）与 Cookie 传递。
 *
 * @author fons
 */
public class SaTokenAuthTemplate {

    /**
     * 登录，写入当前会话的登录账号 id。
     *
     * @param loginId 登录账号 id
     */
    public void login(Object loginId) {
        StpUtil.login(loginId);
    }

    /**
     * 登出当前会话。
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 登出指定账号的所有会话。
     *
     * @param loginId 登录账号 id
     */
    public void logout(Object loginId) {
        StpUtil.logout(loginId);
    }

    /**
     * 当前会话是否已登录。
     *
     * @return 已登录返回 true
     */
    public boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 校验当前会话登录状态，未登录将抛出 {@code NotLoginException}。
     */
    public void checkLogin() {
        StpUtil.checkLogin();
    }

    /**
     * 获取当前会话登录账号 id。
     *
     * @return 登录账号 id
     */
    public Object getCurrentLoginId() {
        return StpUtil.getLoginId();
    }

    /**
     * 获取当前会话登录账号 id 的字符串形式。
     *
     * @return 登录账号 id 字符串
     */
    public String getCurrentLoginIdAsString() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 获取当前会话登录账号 id 的 long 形式。
     *
     * @return 登录账号 id（long）
     */
    public long getCurrentLoginIdAsLong() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前会话的 token 值。
     *
     * @return token 值，未登录返回 null
     */
    public String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    /**
     * 获取当前会话的 token 详细信息。
     *
     * @return token 信息
     */
    public SaTokenInfo getTokenInfo() {
        return StpUtil.getTokenInfo();
    }

    /**
     * 踢人下线：将指定账号的所有会话强制下线。
     *
     * @param loginId 登录账号 id
     */
    public void kickout(Object loginId) {
        StpUtil.kickout(loginId);
    }

    /**
     * 查询指定账号当前所有的 token 值。
     *
     * @param loginId 登录账号 id
     * @return token 值列表
     */
    public List<String> getTokenValueListByLoginId(Object loginId) {
        return StpUtil.getTokenValueListByLoginId(loginId);
    }
}
