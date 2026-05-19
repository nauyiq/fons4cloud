package com.fons.cloud.file.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS 对象响应信息。
 *
 * @author hongqy
 * @date 2026/5/18
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssObjectResponse {

    /**
     * 对象所在 bucket。
     */
    private String bucket;

    /**
     * 对象存储 key。
     */
    private String objectKey;

    /**
     * 对象访问 URL。
     */
    private String accessUrl;

    /**
     * 对象大小，单位字节。
     */
    private Long size;

    /**
     * 对象 ETag。
     */
    private String etag;

    /**
     * 对象内容类型。
     */
    private String contentType;

    /**
     * 对象最后修改时间。
     */
    private Instant lastModified;

    /**
     * 对象元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * 下载对象流。
     */
    private InputStream inputStream;
}
