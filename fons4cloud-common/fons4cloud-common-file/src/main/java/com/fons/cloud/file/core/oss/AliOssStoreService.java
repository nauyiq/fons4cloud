package com.fons.cloud.file.core.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import org.springframework.beans.factory.DisposableBean;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Ali OSS 对象存储服务。
 *
 * @author hongqy
 * @date 2026/5/18
 */
public class AliOssStoreService extends AbstractOssStoreService implements DisposableBean {

    private final OSS ossClient;

    public AliOssStoreService(CloudSecret cloudSecret) {
        super(cloudSecret);
        this.ossClient = createOssClient(cloudSecret);
    }

    AliOssStoreService(CloudSecret cloudSecret, OSS ossClient) {
        super(cloudSecret);
        this.ossClient = ossClient;
    }

    @Override
    protected OssObjectResponse doUpload(String objectKey, OssUploadRequest request) {
        try {
            Map<String, String> userMeta = copyMetadata(request.getMetadata());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(userMeta);
            String contentType = userMeta != null ? userMeta.get("content-type") : null;
            if (contentType != null && !contentType.isBlank()) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(getBucket(), objectKey, request.getInputStream(), metadata);
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            return basicResponse(objectKey)
                    .etag(result.getETag())
                    .metadata(userMeta)
                    .build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public OssObjectResponse download(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            OSSObject object = ossClient.getObject(getBucket(), objectKey);
            InputStream inputStream = object.getObjectContent();
            return fillMetadata(basicResponse(objectKey), object.getObjectMetadata())
                    .inputStream(inputStream)
                    .build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public boolean exists(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            return ossClient.doesObjectExist(getBucket(), objectKey);
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public void delete(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            ossClient.deleteObject(getBucket(), objectKey);
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public OssObjectResponse getObjectInfo(OssObjectRequest request) {
        String objectKey = resolveObjectKey(request);
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(getBucket(), objectKey);
            return fillMetadata(basicResponse(objectKey), metadata).build();
        } catch (Exception ignored) {
            throw operationFailed();
        }
    }

    @Override
    public void destroy() {
        ossClient.shutdown();
    }

    private OSS createOssClient(CloudSecret cloudSecret) {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(cloudSecret.getSecretId(), cloudSecret.getSecretKey());
        return new OSSClientBuilder().build(cloudSecret.getEndpoint(), credentialsProvider);
    }

    private OssObjectResponse.OssObjectResponseBuilder fillMetadata(OssObjectResponse.OssObjectResponseBuilder builder,
                                                                    ObjectMetadata metadata) {
        if (metadata == null) {
            return builder;
        }
        Date lastModified = metadata.getLastModified();
        Map<String, String> userMetadata = metadata.getUserMetadata();
        return builder
                .size(metadata.getContentLength())
                .etag(metadata.getETag())
                .contentType(metadata.getContentType())
                .lastModified(lastModified == null ? null : Instant.ofEpochMilli(lastModified.getTime()))
                .metadata(copyMetadata(userMetadata));
    }
}
