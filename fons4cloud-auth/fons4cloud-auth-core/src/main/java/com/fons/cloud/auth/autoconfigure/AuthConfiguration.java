package com.fons.cloud.auth.autoconfigure;

import com.fons.cloud.auth.api.AuthPermissionService;
import com.fons.cloud.auth.api.support.DefaultAuthPermissionService;
import com.fons.cloud.auth.core.AuthorizationResourceRepository;
import com.fons.cloud.limiter.api.ManualWhiteIpService;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hongqy
 */
@Configuration
public class AuthConfiguration {

    @Bean
    public AuthPermissionService authPermissionService(AuthorizationResourceRepository authorizationResourceRepository, ManualWhiteIpService manualWhiteIpService) {
        return new DefaultAuthPermissionService(authorizationResourceRepository, manualWhiteIpService);
    }
    @Bean
    public AuthorizationResourceRepository authorizationResourceRepository(RedissonClient redissonClient) {
        return new AuthorizationResourceRepository(redissonClient);
    }

}
