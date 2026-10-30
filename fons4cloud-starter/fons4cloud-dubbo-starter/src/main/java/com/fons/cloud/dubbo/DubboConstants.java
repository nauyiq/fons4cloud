package com.fons.cloud.dubbo;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/7/11
 */
public interface DubboConstants {

    String DEFAULT_DUBBO_SERVICE_VERSION = "1.0.0";

    /**
     * Provider 侧响应脱敏开关的 URL 参数名，默认关闭；
     * 通过 @DubboService(parameters = {"fonsSensitive", "true"}) 或全局 provider 配置显式开启
     */
    String SENSITIVE_DESENSITIZE_PARAMETER = "fonsSensitive";

}
