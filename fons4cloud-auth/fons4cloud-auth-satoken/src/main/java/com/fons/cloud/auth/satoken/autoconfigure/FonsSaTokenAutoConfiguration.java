package com.fons.cloud.auth.satoken.autoconfigure;

import cn.dev33.satoken.stp.StpInterface;
import com.fons.cloud.auth.satoken.api.DefaultStpInterfaceImpl;
import com.fons.cloud.auth.satoken.api.SaTokenAuthTemplate;
import com.fons.cloud.auth.satoken.config.FonsSaTokenProperties;
import com.fons.cloud.auth.satoken.config.SaTokenWebMvcConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * fons4cloud 独立认证（Sa-Token）自动配置。
 * <p>
 * 装配认证工具封装、默认权限空实现与路由拦截配置；Sa-Token 原生能力由
 * {@code sa-token-spring-boot3-starter} 自动装配，会话持久化由 {@code sa-token-redis-jackson} 接管。
 *
 * @author fons
 */
@AutoConfiguration
@ConditionalOnClass(name = "cn.dev33.satoken.stp.StpUtil")
@EnableConfigurationProperties(FonsSaTokenProperties.class)
public class FonsSaTokenAutoConfiguration {

    /**
     * 认证工具封装，业务方注入使用登录/登出/会话查询等能力。
     *
     * @return Sa-Token 认证工具
     */
    @Bean
    @ConditionalOnMissingBean
    public SaTokenAuthTemplate saTokenAuthTemplate() {
        return new SaTokenAuthTemplate();
    }

    /**
     * 默认权限/角色空实现，业务方可通过自定义 {@link StpInterface} Bean 覆盖。
     *
     * @return 默认 StpInterface 实现
     */
    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public DefaultStpInterfaceImpl defaultStpInterface() {
        return new DefaultStpInterfaceImpl();
    }

    /**
     * 路由拦截配置，注册 Sa-Token 拦截器并按属性放行白名单。
     *
     * @param properties 扩展配置
     * @return WebMvc 拦截配置
     */
    @Bean
    @ConditionalOnWebApplication
    public SaTokenWebMvcConfigurer saTokenWebMvcConfigurer(FonsSaTokenProperties properties) {
        return new SaTokenWebMvcConfigurer(properties);
    }
}
