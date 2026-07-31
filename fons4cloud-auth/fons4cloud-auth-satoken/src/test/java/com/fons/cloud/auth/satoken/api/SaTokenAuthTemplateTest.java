package com.fons.cloud.auth.satoken.api;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link SaTokenAuthTemplate} 集成测试。
 * <p>
 * 启动最小 Spring Boot Web MOCK 环境，用内存 {@link SaTokenDao} 替代 Redis 持久化，
 * 通过测试 Controller 在 Web 上下文内驱动登录/登出/踢人，验证会话写入与清除。
 * 关闭全局登录校验以放行测试端点。
 *
 * @author fons
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SaTokenAuthTemplateTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "sys.sa-token.global-login-check=false")
class SaTokenAuthTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 最小测试应用：扫描 starter 包装配自动配置，提供内存 SaTokenDao 与测试 Controller。
     */
    @SpringBootApplication(
            scanBasePackages = "com.fons.cloud.auth.satoken",
            exclude = {
                    org.redisson.spring.starter.RedissonAutoConfigurationV2.class,
                    com.fons.cloud.cache.config.IRedisAutoConfiguration.class,
                    com.fons.cloud.cache.config.CacheAutoConfiguration.class
            })
    static class TestApplication {

        /** 内存 SaTokenDao，避免测试依赖 Redis */
        @Bean
        @Primary
        SaTokenDao saTokenDao() {
            return new SaTokenDaoDefaultImpl();
        }

        @RestController
        static class TestController {
            @Autowired
            private SaTokenAuthTemplate template;

            @PostMapping("/__test/login/{id}")
            String login(@PathVariable Long id) {
                template.login(id);
                return template.getTokenValue();
            }

            @GetMapping("/__test/token-count/{id}")
            int tokenCount(@PathVariable Long id) {
                List<String> tokens = template.getTokenValueListByLoginId(id);
                return tokens.size();
            }

            @PostMapping("/__test/kickout/{id}")
            void kickout(@PathVariable Long id) {
                template.kickout(id);
            }

            @PostMapping("/__test/logout/{id}")
            void logout(@PathVariable Long id) {
                template.logout(id);
            }
        }
    }

    @Test
    void shouldLoginAndQueryToken() throws Exception {
        mockMvc.perform(post("/__test/login/{id}", 20001L))
                .andExpect(status().isOk());
        mockMvc.perform(get("/__test/token-count/{id}", 20001L))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not("0")));
    }

    @Test
    void shouldKickoutClearTokens() throws Exception {
        mockMvc.perform(post("/__test/login/{id}", 20002L)).andExpect(status().isOk());
        mockMvc.perform(get("/__test/token-count/{id}", 20002L))
                .andExpect(content().string(Matchers.not("0")));

        mockMvc.perform(post("/__test/kickout/{id}", 20002L)).andExpect(status().isOk());
        mockMvc.perform(get("/__test/token-count/{id}", 20002L))
                .andExpect(content().string("0"));
    }

    @Test
    void shouldLogoutClearTokens() throws Exception {
        mockMvc.perform(post("/__test/login/{id}", 20003L)).andExpect(status().isOk());
        mockMvc.perform(get("/__test/token-count/{id}", 20003L))
                .andExpect(content().string(Matchers.not("0")));

        mockMvc.perform(post("/__test/logout/{id}", 20003L)).andExpect(status().isOk());
        mockMvc.perform(get("/__test/token-count/{id}", 20003L))
                .andExpect(content().string("0"));
    }
}
