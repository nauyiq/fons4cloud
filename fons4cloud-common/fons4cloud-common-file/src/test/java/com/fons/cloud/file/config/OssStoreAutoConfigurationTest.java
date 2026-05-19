package com.fons.cloud.file.config;

import com.fons.cloud.file.api.OssStoreService;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import com.fons.cloud.file.core.oss.AliOssStoreService;
import com.fons.cloud.file.core.oss.MinioOssStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OssStoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OssStoreAutoConfiguration.class));

    @Test
    void shouldNotRegisterOssStoreServiceWhenDisabled() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(OssStoreService.class));
    }

    @Test
    void shouldRegisterAliOssStoreServiceWhenEnabledWithoutProvider() {
        contextRunner
                .withPropertyValues(
                        "fons4cloud.upload.oss.enabled=true",
                        "fons4cloud.upload.oss.endpoint=https://oss.example.com",
                        "fons4cloud.upload.oss.bucket=test-bucket",
                        "fons4cloud.upload.oss.secret-id=test-secret-id",
                        "fons4cloud.upload.oss.secret-key=test-secret-key")
                .run(context -> assertThat(context).hasSingleBean(AliOssStoreService.class));
    }

    @Test
    void shouldRegisterMinioOssStoreServiceWhenProviderIsMinio() {
        contextRunner
                .withPropertyValues(
                        "fons4cloud.upload.oss.enabled=true",
                        "fons4cloud.upload.oss.provider=MINIO",
                        "fons4cloud.upload.oss.endpoint=https://minio.example.com",
                        "fons4cloud.upload.oss.bucket=test-bucket",
                        "fons4cloud.upload.oss.secret-id=test-secret-id",
                        "fons4cloud.upload.oss.secret-key=test-secret-key")
                .run(context -> assertThat(context).hasSingleBean(MinioOssStoreService.class));
    }

    @Test
    void shouldFailWithoutLeakingSecretWhenRequiredConfigMissing() {
        contextRunner
                .withPropertyValues(
                        "fons4cloud.upload.oss.enabled=true",
                        "fons4cloud.upload.oss.bucket=test-bucket",
                        "fons4cloud.upload.oss.secret-id=leaked-secret-id",
                        "fons4cloud.upload.oss.secret-key=leaked-secret-key")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("fons4cloud.upload.oss")
                            .hasMessageNotContaining("leaked-secret-id")
                            .hasMessageNotContaining("leaked-secret-key");
                });
    }

    @Test
    void shouldFailWhenTencentProviderIsConfigured() {
        contextRunner
                .withPropertyValues(
                        "fons4cloud.upload.oss.enabled=true",
                        "fons4cloud.upload.oss.provider=TENCENT_OSS",
                        "fons4cloud.upload.oss.endpoint=https://cos.example.com",
                        "fons4cloud.upload.oss.bucket=test-bucket",
                        "fons4cloud.upload.oss.secret-id=test-secret-id",
                        "fons4cloud.upload.oss.secret-key=test-secret-key")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("TENCENT_OSS")
                            .hasMessageNotContaining("test-secret-id")
                            .hasMessageNotContaining("test-secret-key");
                });
    }

    @Test
    void shouldNotOverrideCustomOssStoreService() {
        contextRunner
                .withUserConfiguration(CustomOssStoreServiceConfiguration.class)
                .withPropertyValues("fons4cloud.upload.oss.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OssStoreService.class)
                        .getBean(OssStoreService.class)
                        .isSameAs(context.getBean("customOssStoreService")));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOssStoreServiceConfiguration {

        @Bean
        OssStoreService customOssStoreService() {
            return new OssStoreService() {
                @Override
                public OssObjectResponse upload(OssUploadRequest request) {
                    return null;
                }

                @Override
                public OssObjectResponse download(OssObjectRequest request) {
                    return null;
                }

                @Override
                public boolean exists(OssObjectRequest request) {
                    return false;
                }

                @Override
                public void delete(OssObjectRequest request) {
                }

                @Override
                public OssObjectResponse getObjectInfo(OssObjectRequest request) {
                    return null;
                }

                @Override
                public String getAccessUrl(OssObjectRequest request) {
                    return null;
                }
            };
        }
    }
}
