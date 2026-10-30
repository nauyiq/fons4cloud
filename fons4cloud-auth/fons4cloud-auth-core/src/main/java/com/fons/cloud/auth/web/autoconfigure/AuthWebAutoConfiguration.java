package com.fons.cloud.auth.web.autoconfigure;

import com.fons.cloud.auth.core.AuthorizationResourceRepository;
import com.fons.cloud.auth.web.filter.AuthUserFilter;
import com.fons.cloud.auth.web.filter.TokenFilter;
import com.fons.cloud.auth.web.support.HttpAuthenticationResourceScanner;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 框架认证路径的Web集成自动配置：注册HTTP权限资源扫描器与认证相关Servlet过滤器。
 * <p>依赖 {@code AuthConfiguration} 自动装配的 AuthorizationResourceRepository（需要 RedissonClient），
 * 适用于接入网关认证体系的服务；Sa-Token 独立认证应用不引入 auth-core。</p>
 * <p>仅 Servlet 环境装配：网关为 reactive 应用（经 auth-spring-security 依赖本模块），
 * 条件不满足时全部 bean 跳过，不向 reactive 上下文注册无效的 Servlet 过滤器。</p>
 *
 * @author hongqy
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthWebAutoConfiguration {


    /**
     * HTTP权限资源注解扫描器
     * <p>启动时扫描 @AuthenticationResource 与 @BsWebAdvice，将权限资源与 token
     * 访问要求注册到认证资源仓库。仅通过本 @Bean 注册一次（修正原 common-web 中
     * imports 与 @Bean 的双重注册）。</p>
     *
     * @param repository 认证资源仓库
     * @return           HTTP权限资源扫描器
     */
    @Bean
    public HttpAuthenticationResourceScanner httpAuthenticationResourceScanner(AuthorizationResourceRepository repository) {
        return new HttpAuthenticationResourceScanner(repository);
    }

    /**
     * 注册TOKEN 过滤器
     * <p>拦截范围取仓库中的幂等 token URI；无注册 URI 时禁用。</p>
     * @param repository     认证资源仓库，读取幂等 token URI
     * @param redissonClient redisson客户端
     * @return               注册bean
     */
    @Bean
    public FilterRegistrationBean<TokenFilter> tokenFilter(AuthorizationResourceRepository repository, RedissonClient redissonClient) {
        FilterRegistrationBean<TokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TokenFilter(redissonClient));
        // 获取需要校验token的url pattern
        String[] array = repository.getIdentifierTokenUri().toArray(new String[0]);
        if (array.length > 0) {
            registrationBean.addUrlPatterns(array);
        } else {
            registrationBean.setEnabled(false);
        }
        registrationBean.setOrder(10);
        return registrationBean;
    }

    /**
     * 注册AUTHUser 过滤器
     * <p>全路径挂载，请求结束清理 ThreadLocal 认证用户，防止线程池复用导致上下文泄漏。</p>
     * @return 注册bean
     */
    @Bean
    public FilterRegistrationBean<AuthUserFilter> authUserFilter() {
        FilterRegistrationBean<AuthUserFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AuthUserFilter());
        bean.addUrlPatterns("/**");
        bean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return bean;
    }

}
