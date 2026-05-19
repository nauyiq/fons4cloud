package com.fons.cloud.file.core.oss;

import com.fons.cloud.file.common.CloudSecret;
import com.fons.cloud.file.common.FileException;
import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractOssStoreServiceTest {

    @Test
    void shouldUseExplicitObjectKeyWhenUpload() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());
        OssUploadRequest request = uploadRequest()
                .setObjectKey("/custom/path/demo.txt");

        OssObjectResponse response = service.upload(request);

        assertThat(response.getObjectKey()).isEqualTo("custom/path/demo.txt");
        assertThat(service.uploadedObjectKey).isEqualTo("custom/path/demo.txt");
    }

    @Test
    void shouldGenerateObjectKeyWhenObjectKeyMissing() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());

        OssObjectResponse response = service.upload(uploadRequest()
                .setScene("avatar")
                .setAccessUniqueId("user-1")
                .setFilename("profile.png"));

        assertThat(response.getObjectKey())
                .matches("\\d{4}-\\d{2}-\\d{2}/avatar/user-1/[0-9a-f]{32}\\.png");
    }

    @Test
    void shouldOmitAccessUniqueIdWhenGenerateObjectKey() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());

        OssObjectResponse response = service.upload(uploadRequest()
                .setScene("avatar")
                .setAccessUniqueId(null)
                .setFilename("profile.png"));

        assertThat(response.getObjectKey())
                .matches("\\d{4}-\\d{2}-\\d{2}/avatar/[0-9a-f]{32}\\.png");
    }

    @Test
    void shouldResolveAccessUriToObjectKey() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());
        OssObjectRequest request = new OssObjectRequest()
                .setAccessUri("https://oss.example.com/test-bucket//avatar/user-1/profile.png");

        String accessUrl = service.getAccessUrl(request);

        assertThat(accessUrl).isEqualTo("https://oss.example.com/avatar/user-1/profile.png");
    }

    @Test
    void shouldPreferObjectKeyOverAccessUri() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());
        OssObjectRequest request = new OssObjectRequest()
                .setObjectKey("/preferred/object.txt")
                .setAccessUri("https://oss.example.com/test-bucket/ignored/object.txt");

        String accessUrl = service.getAccessUrl(request);

        assertThat(accessUrl).isEqualTo("https://oss.example.com/preferred/object.txt");
    }

    @Test
    void shouldPassMetadataToProviderUpload() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());
        Map<String, String> metadata = new HashMap<>();
        metadata.put("traceId", "trace-1");

        service.upload(uploadRequest().setMetadata(metadata));

        assertThat(service.uploadedMetadata).containsEntry("traceId", "trace-1");
    }

    @Test
    void shouldRejectNullInputStream() {
        TestOssStoreService service = new TestOssStoreService(cloudSecret());

        assertThatThrownBy(() -> service.upload(uploadRequest().setInputStream(null)))
                .isInstanceOfSatisfying(FileException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("FILE_IS_EMPTY"));
    }

    private OssUploadRequest uploadRequest() {
        return new OssUploadRequest()
                .setScene("common")
                .setFilename("demo.txt")
                .setInputStream(new ByteArrayInputStream("demo".getBytes()));
    }

    private CloudSecret cloudSecret() {
        CloudSecret cloudSecret = new CloudSecret();
        cloudSecret.setEndpoint("https://oss.example.com");
        cloudSecret.setBucket("test-bucket");
        cloudSecret.setSecretId("test-secret-id");
        cloudSecret.setSecretKey("test-secret-key");
        return cloudSecret;
    }

    private static class TestOssStoreService extends AbstractOssStoreService {

        private String uploadedObjectKey;
        private Map<String, String> uploadedMetadata;

        private TestOssStoreService(CloudSecret cloudSecret) {
            super(cloudSecret);
        }

        @Override
        protected OssObjectResponse doUpload(String objectKey, OssUploadRequest request) {
            this.uploadedObjectKey = objectKey;
            this.uploadedMetadata = request.getMetadata();
            return basicResponse(objectKey).metadata(request.getMetadata()).build();
        }

        @Override
        public OssObjectResponse download(OssObjectRequest request) {
            return basicResponse(resolveObjectKey(request)).build();
        }

        @Override
        public boolean exists(OssObjectRequest request) {
            return true;
        }

        @Override
        public void delete(OssObjectRequest request) {
        }

        @Override
        public OssObjectResponse getObjectInfo(OssObjectRequest request) {
            return basicResponse(resolveObjectKey(request)).build();
        }
    }
}
