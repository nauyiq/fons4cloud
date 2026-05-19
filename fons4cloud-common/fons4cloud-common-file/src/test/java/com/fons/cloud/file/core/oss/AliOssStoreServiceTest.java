package com.fons.cloud.file.core.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AliOssStoreServiceTest {

    @Test
    void shouldUploadObjectWithMetadata() {
        OSS ossClient = mock(OSS.class);
        PutObjectResult putObjectResult = new PutObjectResult();
        putObjectResult.setETag("etag-1");
        when(ossClient.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);
        AliOssStoreService service = new AliOssStoreService(cloudSecret(), ossClient);

        OssObjectResponse response = service.upload(uploadRequest().setMetadata(Map.of("traceId", "trace-1")));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(ossClient).putObject(captor.capture());
        PutObjectRequest request = captor.getValue();
        assertThat(request.getBucketName()).isEqualTo("test-bucket");
        assertThat(request.getKey()).isEqualTo("folder/demo.txt");
        assertThat(request.getMetadata().getUserMetadata()).containsEntry("traceId", "trace-1");
        assertThat(response.getObjectKey()).isEqualTo("folder/demo.txt");
        assertThat(response.getEtag()).isEqualTo("etag-1");
    }

    @Test
    void shouldDownloadObject() {
        OSS ossClient = mock(OSS.class);
        OSSObject ossObject = mock(OSSObject.class);
        InputStream objectInputStream = mock(InputStream.class);
        ObjectMetadata metadata = objectMetadata();
        when(ossObject.getObjectContent()).thenReturn(objectInputStream);
        when(ossObject.getObjectMetadata()).thenReturn(metadata);
        when(ossClient.getObject("test-bucket", "folder/demo.txt")).thenReturn(ossObject);
        AliOssStoreService service = new AliOssStoreService(cloudSecret(), ossClient);

        OssObjectResponse response = service.download(new OssObjectRequest().setObjectKey("folder/demo.txt"));

        assertThat(response.getInputStream()).isSameAs(objectInputStream);
        assertThat(response.getSize()).isEqualTo(4L);
        assertThat(response.getContentType()).isEqualTo("text/plain");
    }

    @Test
    void shouldCheckExistsDeleteAndGetObjectInfo() {
        OSS ossClient = mock(OSS.class);
        when(ossClient.doesObjectExist("test-bucket", "folder/demo.txt")).thenReturn(true);
        when(ossClient.getObjectMetadata("test-bucket", "folder/demo.txt")).thenReturn(objectMetadata());
        AliOssStoreService service = new AliOssStoreService(cloudSecret(), ossClient);

        assertThat(service.exists(new OssObjectRequest().setObjectKey("folder/demo.txt"))).isTrue();
        service.delete(new OssObjectRequest().setObjectKey("folder/demo.txt"));
        OssObjectResponse info = service.getObjectInfo(new OssObjectRequest().setObjectKey("folder/demo.txt"));

        verify(ossClient).deleteObject("test-bucket", "folder/demo.txt");
        assertThat(info.getAccessUrl()).isEqualTo("https://oss.example.com/folder/demo.txt");
        assertThat(info.getMetadata()).containsEntry("traceId", "trace-1");
    }

    @Test
    void shouldShutdownAliClientWhenDestroy() {
        OSS ossClient = mock(OSS.class);
        AliOssStoreService service = new AliOssStoreService(cloudSecret(), ossClient);

        service.destroy();

        verify(ossClient).shutdown();
    }

    @Test
    void shouldNotLeakSecretWhenSdkThrowsException() {
        OSS ossClient = mock(OSS.class);
        when(ossClient.putObject(any(PutObjectRequest.class)))
                .thenThrow(new IllegalStateException("sdk failed with test-secret-id/test-secret-key"));
        AliOssStoreService service = new AliOssStoreService(cloudSecret(), ossClient);

        assertThatThrownBy(() -> service.upload(uploadRequest()))
                .hasMessageNotContaining("test-secret-id")
                .hasMessageNotContaining("test-secret-key");
    }

    private OssUploadRequest uploadRequest() {
        return new OssUploadRequest()
                .setObjectKey("folder/demo.txt")
                .setScene("common")
                .setFilename("demo.txt")
                .setInputStream(new ByteArrayInputStream("demo".getBytes()));
    }

    private ObjectMetadata objectMetadata() {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(4L);
        metadata.setContentType("text/plain");
        metadata.setLastModified(new Date());
        metadata.setUserMetadata(Map.of("traceId", "trace-1"));
        return metadata;
    }

    private CloudSecret cloudSecret() {
        CloudSecret cloudSecret = new CloudSecret();
        cloudSecret.setEndpoint("https://oss.example.com");
        cloudSecret.setBucket("test-bucket");
        cloudSecret.setSecretId("test-secret-id");
        cloudSecret.setSecretKey("test-secret-key");
        return cloudSecret;
    }
}
