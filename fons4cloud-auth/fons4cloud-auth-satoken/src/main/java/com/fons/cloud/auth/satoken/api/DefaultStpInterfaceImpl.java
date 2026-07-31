package com.fons.cloud.auth.satoken.api;

import cn.dev33.satoken.stp.StpInterface;

import java.util.Collections;
import java.util.List;

/**
 * {@link StpInterface} 的默认空实现。
 * <p>
 * 不提供任何权限/角色数据，业务方实现自己的 {@link StpInterface} Bean 即可覆盖，
 * 从而接入 {@code @SaCheckPermission}、{@code @SaCheckRole} 注解鉴权。
 *
 * @author fons
 */
public class DefaultStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
