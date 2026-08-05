package com.fons.cloud.elasticsearch.config;

import com.fons.cloud.util.config.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author hongqy
 */
@Configuration
@PropertySource(value = "classpath:elastic.yml", factory = YamlPropertySourceFactory.class)
public class ElasticAutoConfiguration {
}
