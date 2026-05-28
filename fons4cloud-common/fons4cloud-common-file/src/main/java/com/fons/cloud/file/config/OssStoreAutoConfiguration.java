package com.fons.cloud.file.config;

import com.fons.cloud.file.api.OssStoreService;
import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.FileException;
import com.fons.cloud.file.common.constants.ServerProvider;
import com.fons.cloud.file.core.oss.AliOssStoreService;
import com.fons.cloud.file.core.oss.MinioOssStoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 对象存储自动配置。
 *
 * @author hongqy
 * @date 2026/5/18
 */
@Configuration
@EnableConfigurationProperties({UploadFileProperties.class, CloudSecret.class})
@ConditionalOnProperty(prefix = "fons4cloud.upload.oss", name = "enabled", havingValue = "true")
public class OssStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OssStoreService.class)
    public OssStoreService ossStoreService(UploadFileProperties properties) {
        CloudSecret cloudSecret = properties.getOss();
        ServerProvider provider = cloudSecret == null || cloudSecret.getProvider() == null
                ? ServerProvider.ALI_OSS
                : cloudSecret.getProvider();
        return switch (provider) {
            case ALI_OSS -> new AliOssStoreService(cloudSecret);
            case MINIO -> new MinioOssStoreService(cloudSecret);
            case TENCENT_OSS -> throw new FileException("OSS_PROVIDER_NOT_SUPPORTED",
                    "TENCENT_OSS is not supported by OssStoreService");
        };
    }
}
