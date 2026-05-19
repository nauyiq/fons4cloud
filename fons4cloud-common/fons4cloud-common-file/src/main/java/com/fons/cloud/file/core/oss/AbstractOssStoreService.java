package com.fons.cloud.file.core.oss;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.fons.cloud.file.api.OssStoreService;
import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.FileException;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import com.fons.cloud.file.common.result.FileResultCode;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS 对象存储共享基类。
 *
 * @author hongqy
 * @date 2026/5/18
 */
public abstract class AbstractOssStoreService implements OssStoreService {

    private static final String OSS_CONFIG_ERROR_CODE = "OSS_CONFIG_INVALID";
    private static final String OSS_OBJECT_ERROR_CODE = "OSS_OBJECT_INVALID";
    private static final String OSS_OPERATION_ERROR_CODE = "OSS_OPERATION_FAILED";
    private static final String OSS_CONFIG_ERROR_MESSAGE = "fons4cloud.upload.oss config is incomplete";
    private static final String OSS_OBJECT_ERROR_MESSAGE = "OSS objectKey or accessUri is required";
    private static final String OSS_OPERATION_ERROR_MESSAGE = "OSS operation failed";

    private final CloudSecret cloudSecret;
    private final String endpoint;

    protected AbstractOssStoreService(CloudSecret cloudSecret) {
        validateCloudSecret(cloudSecret);
        this.cloudSecret = cloudSecret;
        this.endpoint = normalizeEndpoint(cloudSecret.getEndpoint());
    }

    @Override
    public OssObjectResponse upload(OssUploadRequest request) {
        if (request == null || request.getInputStream() == null) {
            throw new FileException(FileResultCode.FILE_IS_EMPTY);
        }
        String objectKey = resolveUploadObjectKey(request);
        request.setObjectKey(objectKey);
        request.setMetadata(copyMetadata(request.getMetadata()));
        return doUpload(objectKey, request);
    }

    @Override
    public String getAccessUrl(OssObjectRequest request) {
        return buildAccessUrl(resolveObjectKey(request));
    }

    /**
     * provider 执行真实上传。共享基类负责 key 生成、元数据复制和空流校验。
     *
     * @param objectKey 规范化后的 objectKey
     * @param request 上传请求
     * @return 对象响应
     */
    protected abstract OssObjectResponse doUpload(String objectKey, OssUploadRequest request);

    protected CloudSecret getCloudSecret() {
        return cloudSecret;
    }

    protected String getEndpoint() {
        return endpoint;
    }

    protected String getBucket() {
        return cloudSecret.getBucket();
    }

    protected String resolveObjectKey(OssObjectRequest request) {
        if (request == null) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, OSS_OBJECT_ERROR_MESSAGE);
        }
        if (StringUtils.isNotBlank(request.getObjectKey())) {
            return normalizeObjectKey(request.getObjectKey());
        }
        if (StringUtils.isBlank(request.getAccessUri())) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, OSS_OBJECT_ERROR_MESSAGE);
        }
        return resolveObjectKeyFromAccessUri(request.getAccessUri());
    }

    protected String buildAccessUrl(String objectKey) {
        return endpoint + "/" + normalizeObjectKey(objectKey);
    }

    protected OssObjectResponse.OssObjectResponseBuilder basicResponse(String objectKey) {
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        return OssObjectResponse.builder()
                .bucket(getBucket())
                .objectKey(normalizedObjectKey)
                .accessUrl(buildAccessUrl(normalizedObjectKey))
                .metadata(new HashMap<>());
    }

    protected FileException operationFailed() {
        return new FileException(OSS_OPERATION_ERROR_CODE, OSS_OPERATION_ERROR_MESSAGE);
    }

    protected Map<String, String> copyMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(metadata);
    }

    private String resolveUploadObjectKey(OssUploadRequest request) {
        if (StringUtils.isNotBlank(request.getObjectKey())) {
            return normalizeObjectKey(request.getObjectKey());
        }
        String scene = normalizePathSegment(request.getScene());
        if (StringUtils.isBlank(scene)) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, "OSS upload scene is required");
        }
        String generatedFilename = buildGeneratedFilename(request.getFilename());
        String accessUniqueId = normalizePathSegment(request.getAccessUniqueId());
        if (StringUtils.isBlank(accessUniqueId)) {
            return String.join("/", DateUtil.today(), scene, generatedFilename);
        }
        return String.join("/", DateUtil.today(), scene, accessUniqueId, generatedFilename);
    }

    private String buildGeneratedFilename(String filename) {
        String suffix = FileNameUtil.getSuffix(filename);
        if (StringUtils.isBlank(suffix)) {
            return IdUtil.fastSimpleUUID();
        }
        return IdUtil.fastSimpleUUID() + "." + suffix;
    }

    private String resolveObjectKeyFromAccessUri(String accessUri) {
        String candidate = StringUtils.trimToEmpty(accessUri);
        if (candidate.startsWith(endpoint)) {
            candidate = candidate.substring(endpoint.length());
        } else {
            candidate = parseUriPath(candidate);
        }
        String objectKey = normalizeObjectKey(candidate);
        String bucket = normalizeObjectKey(getBucket());
        if (objectKey.equals(bucket)) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, OSS_OBJECT_ERROR_MESSAGE);
        }
        if (objectKey.startsWith(bucket + "/")) {
            objectKey = objectKey.substring(bucket.length() + 1);
        }
        if (StringUtils.isBlank(objectKey)) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, OSS_OBJECT_ERROR_MESSAGE);
        }
        return objectKey;
    }

    private String parseUriPath(String accessUri) {
        try {
            URI uri = URI.create(accessUri);
            if (StringUtils.isNotBlank(uri.getScheme()) && StringUtils.isNotBlank(uri.getHost())) {
                return uri.getPath();
            }
        } catch (IllegalArgumentException ignored) {
            // 非法 URI 按普通 objectKey 处理，避免把 accessUri 解析失败扩大成启动风险。
        }
        return accessUri;
    }

    private String normalizePathSegment(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return normalizeObjectKey(value);
    }

    protected String normalizeObjectKey(String objectKey) {
        String normalized = StringUtils.trimToEmpty(objectKey).replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        normalized = normalized.replaceAll("/{2,}", "/");
        if (StringUtils.isBlank(normalized)) {
            throw new FileException(OSS_OBJECT_ERROR_CODE, OSS_OBJECT_ERROR_MESSAGE);
        }
        return normalized;
    }

    private String normalizeEndpoint(String endpoint) {
        String normalized = StringUtils.trimToEmpty(endpoint);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private void validateCloudSecret(CloudSecret cloudSecret) {
        if (cloudSecret == null
                || StringUtils.isBlank(cloudSecret.getEndpoint())
                || StringUtils.isBlank(cloudSecret.getBucket())
                || StringUtils.isBlank(cloudSecret.getSecretId())
                || StringUtils.isBlank(cloudSecret.getSecretKey())) {
            throw new FileException(OSS_CONFIG_ERROR_CODE, OSS_CONFIG_ERROR_MESSAGE);
        }
    }
}
