package com.fons.cloud.file.config;

import com.fons.cloud.file.api.FileService;
import com.fons.cloud.file.api.UploadFileService;
import com.fons.cloud.file.check.FileUploadCheckerChain;
import com.fons.cloud.file.check.support.FileUploadTypeChecker;
import com.fons.cloud.file.core.DefaultUploadFileService;
import com.fons.cloud.file.core.oss.AliCloudFileService;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;


/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/1/4 14:27
 */
@Configuration
@EnableConfigurationProperties(UploadFileProperties.class)
public class UploadFileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MultipartConfigElement multipartConfigElement(UploadFileProperties properties) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        DataSize maxRequestSize = DataSize.ofBytes(properties.getMaxSize().toMillis());
        //文件最大
        factory.setMaxFileSize(maxRequestSize);
        //设置总上传数据总大小
        factory.setMaxRequestSize(maxRequestSize);
        return factory.createMultipartConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileUploadCheckerChain fileUploadCheckerChain() {
        // 文件类型检查
        FileUploadTypeChecker fileUploadTypeChecker = new FileUploadTypeChecker();
        return new FileUploadCheckerChain()
                .addChecker(fileUploadTypeChecker);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileService fileService() {
        return new AliCloudFileService();
    }

    @Bean
    @ConditionalOnMissingBean
    public UploadFileService uploadFileService(UploadFileProperties uploadFileProperties) {
        return new DefaultUploadFileService(uploadFileProperties);
    }



}
