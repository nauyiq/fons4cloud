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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioOssStoreServiceTest {

    @Test
    void shouldUploadObjectWithMetadata() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        ObjectWriteResponse writeResponse = mock(ObjectWriteResponse.class);
        when(writeResponse.etag()).thenReturn("etag-1");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(writeResponse);
        MinioOssStoreService service = new MinioOssStoreService(cloudSecret(), minioClient);

        OssObjectResponse response = service.upload(uploadRequest().setMetadata(Map.of("traceId", "trace-1")));

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("test-bucket");
        assertThat(args.object()).isEqualTo("folder/demo.txt");
        assertThat(args.userMetadata().values()).contains("trace-1");
        assertThat(response.getEtag()).isEqualTo("etag-1");
    }

    @Test
    void shouldDownloadObject() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        GetObjectResponse objectResponse = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(objectResponse);
        MinioOssStoreService service = new MinioOssStoreService(cloudSecret(), minioClient);

        OssObjectResponse response = service.download(new OssObjectRequest().setObjectKey("folder/demo.txt"));

        assertThat(response.getInputStream()).isSameAs(objectResponse);
        ArgumentCaptor<GetObjectArgs> captor = ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(minioClient).getObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("folder/demo.txt");
    }

    @Test
    void shouldCheckExistsDeleteAndGetObjectInfo() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        StatObjectResponse statObjectResponse = mock(StatObjectResponse.class);
        when(statObjectResponse.size()).thenReturn(4L);
        when(statObjectResponse.etag()).thenReturn("etag-1");
        when(statObjectResponse.contentType()).thenReturn("text/plain");
        when(statObjectResponse.lastModified()).thenReturn(ZonedDateTime.now());
        when(statObjectResponse.userMetadata()).thenReturn(Map.of("traceId", "trace-1"));
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        MinioOssStoreService service = new MinioOssStoreService(cloudSecret(), minioClient);

        assertThat(service.exists(new OssObjectRequest().setObjectKey("folder/demo.txt"))).isTrue();
        service.delete(new OssObjectRequest().setObjectKey("folder/demo.txt"));
        OssObjectResponse info = service.getObjectInfo(new OssObjectRequest().setObjectKey("folder/demo.txt"));

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertThat(info.getAccessUrl()).isEqualTo("https://minio.example.com/folder/demo.txt");
        assertThat(info.getMetadata()).containsEntry("traceId", "trace-1");
    }

    @Test
    void shouldNotLeakSecretWhenSdkThrowsException() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        doThrow(new IllegalStateException("sdk failed with test-secret-id/test-secret-key"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));
        MinioOssStoreService service = new MinioOssStoreService(cloudSecret(), minioClient);

        assertThatThrownBy(() -> service.delete(new OssObjectRequest().setObjectKey("folder/demo.txt")))
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

    private CloudSecret cloudSecret() {
        CloudSecret cloudSecret = new CloudSecret();
        cloudSecret.setEndpoint("https://minio.example.com");
        cloudSecret.setBucket("test-bucket");
        cloudSecret.setSecretId("test-secret-id");
        cloudSecret.setSecretKey("test-secret-key");
        return cloudSecret;
    }
}
