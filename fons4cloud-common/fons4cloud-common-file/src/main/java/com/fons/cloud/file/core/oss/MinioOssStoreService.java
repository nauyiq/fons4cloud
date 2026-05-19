package com.fons.cloud.file.core.oss;

import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * MinIO 对象存储服务。
 *
 * @author hongqy
 * @date 2026/5/18
 */
public class MinioOssStoreService extends AbstractOssStoreService {

    private static final long UNKNOWN_OBJECT_SIZE = -1L;
    private static final long DEFAULT_PART_SIZE = 10L * 1024L * 1024L;

    private final MinioClient minioClient;

    public MinioOssStoreService(CloudSecret cloudSecret) {
        super(cloudSecret);
        this.minioClient = MinioClient.builder()
                .endpoint(getEndpoint())
                .credentials(cloudSecret.getSecretId(), cloudSecret.getSecretKey())
                .build();
    }

    MinioOssStoreService(CloudSecret cloudSecret, MinioClient minioClient) {
        super(cloudSecret);
        this.minioClient = minioClient;
    }

    @Override
    protected OssObjectResponse doUpload(String objectKey, OssUploadRequest request) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(getBucket())
                    .object(objectKey)
                    .userMetadata(copyMetadata(request.getMetadata()))
                    .stream(request.getInputStream(), UNKNOWN_OBJECT_SIZE, DEFAULT_PART_SIZE)
                    .build();
            ObjectWriteResponse response = minioClient.putObject(putObjectArgs);
            return basicResponse(objectKey)
                    .etag(response.etag())
                    .metadata(copyMetadata(request.getMetadata()))
                    .build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public OssObjectResponse download(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(getBucket())
                    .object(objectKey)
                    .build());
            return basicResponse(objectKey)
                    .inputStream(response)
                    .build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public boolean exists(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(getBucket())
                    .object(objectKey)
                    .build());
            return true;
        } catch (ErrorResponseException exception) {
            if (isObjectNotFound(exception)) {
                return false;
            }
            throw operationFailed();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public void delete(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public OssObjectResponse getObjectInfo(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(getBucket())
                    .object(objectKey)
                    .build());
            ZonedDateTime lastModified = response.lastModified();
            Map<String, String> metadata = response.userMetadata();
            return basicResponse(objectKey)
                    .size(response.size())
                    .etag(response.etag())
                    .contentType(response.contentType())
                    .lastModified(lastModified == null ? null : lastModified.toInstant())
                    .metadata(copyMetadata(metadata))
                    .build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    private boolean isObjectNotFound(ErrorResponseException exception) {
        String code = exception.errorResponse() == null ? null : exception.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "NotFound".equals(code);
    }
}
