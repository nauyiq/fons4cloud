package com.fons.cloud.file.common.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS 对象上传请求。
 *
 * @author hongqy
 * @date 2026/5/18
 */
@Getter
@Setter
@ToString(exclude = "inputStream")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OssUploadRequest {

    /**
     * 对象存储 key。显式传入时优先使用，不再生成默认路径。
     */
    private String objectKey;

    /**
     * 上传场景，用于默认 objectKey 的路径分组。
     */
    private String scene;

    /**
     * 访问唯一 ID，用于默认 objectKey 的可选路径分组。
     */
    private String accessUniqueId;

    /**
     * 原始文件名，用于提取后缀并生成默认 objectKey 文件名。
     */
    private String filename;

    /**
     * 上传对象流。
     */
    private InputStream inputStream;

    /**
     * 对象元数据，会透传给 OSS SDK。
     */
    private Map<String, String> metadata = new HashMap<>();
}
