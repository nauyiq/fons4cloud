package com.fons.cloud.file.common.request;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * OSS 对象定位请求。
 *
 * @author hongqy
 * @date 2026/5/18
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssObjectRequest {

    /**
     * 对象存储 key，优先级高于 accessUri。
     */
    private String objectKey;

    /**
     * 对象访问地址。objectKey 为空时从该字段兜底解析。
     */
    private String accessUri;
}
