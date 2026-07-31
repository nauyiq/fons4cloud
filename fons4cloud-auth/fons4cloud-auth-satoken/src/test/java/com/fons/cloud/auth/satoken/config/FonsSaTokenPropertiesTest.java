package com.fons.cloud.auth.satoken.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FonsSaTokenProperties} 默认值与绑定测试。
 *
 * @author fons
 */
class FonsSaTokenPropertiesTest {

    @Test
    void shouldUseDefaults() {
        FonsSaTokenProperties properties = new FonsSaTokenProperties();
        assertTrue(properties.isGlobalLoginCheck(), "默认应开启全局登录校验");
        assertEquals(List.of("/**"), properties.getIncludePaths(), "默认拦截全部路径");
        assertNotNull(properties.getExcludePaths());
        assertTrue(properties.getExcludePaths().isEmpty(), "默认放行路径为空");
    }

    @Test
    void shouldBindCustomValues() {
        FonsSaTokenProperties properties = new FonsSaTokenProperties();
        properties.setGlobalLoginCheck(false);
        properties.setIncludePaths(List.of("/api/**"));
        properties.setExcludePaths(List.of("/login", "/actuator/**"));

        assertFalse(properties.isGlobalLoginCheck());
        assertEquals(List.of("/api/**"), properties.getIncludePaths());
        assertEquals(List.of("/login", "/actuator/**"), properties.getExcludePaths());
    }
}
