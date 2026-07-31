package com.fons.cloud.auth.satoken.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultStpInterfaceImpl} 默认空实现测试。
 *
 * @author fons
 */
class DefaultStpInterfaceImplTest {

    private final DefaultStpInterfaceImpl stpInterface = new DefaultStpInterfaceImpl();

    @Test
    void shouldReturnEmptyPermissions() {
        assertTrue(stpInterface.getPermissionList(10001L, "login").isEmpty(),
                "默认实现应返回空权限列表");
    }

    @Test
    void shouldReturnEmptyRoles() {
        assertTrue(stpInterface.getRoleList(10001L, "login").isEmpty(),
                "默认实现应返回空角色列表");
    }
}
