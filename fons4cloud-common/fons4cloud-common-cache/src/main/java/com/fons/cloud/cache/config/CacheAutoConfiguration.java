package com.fons.cloud.cache.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.fons.cloud.util.config.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 缓存配置类
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/6/18
 */
@Configuration
@EnableMethodCache(basePackages = "com.fons.cloud")
@PropertySource(value = "classpath:cache.yml", factory = YamlPropertySourceFactory.class)
public class CacheAutoConfiguration {




}
