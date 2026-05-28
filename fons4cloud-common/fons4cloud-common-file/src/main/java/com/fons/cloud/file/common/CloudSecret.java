package com.fons.cloud.file.common;

import cn.hutool.core.map.MapUtil;
import com.fons.cloud.file.common.constants.ServerProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/5/25 10:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("fons4cloud.upload.oss")
public class CloudSecret {

    /**
     * 是否启用新的 OSS 对象存储服务自动配置。
     */
    private Boolean enabled = false;

    /**
     * OSS 服务提供商，默认使用 Ali OSS。
     */
    private ServerProvider provider = ServerProvider.ALI_OSS;

    private String appId;
    private String secretId;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private Map<String, String> properties = MapUtil.newHashMap(4);
}
