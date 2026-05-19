package com.fons.cloud.file.api;

import com.fons.cloud.file.common.request.OssObjectRequest;
import com.fons.cloud.file.common.request.OssUploadRequest;
import com.fons.cloud.file.common.response.OssObjectResponse;

/**
 * OSS 对象存储服务。
 *
 * @author hongqy
 * @date 2026/5/18
 */
public interface OssStoreService {

    /**
     * 上传对象。
     *
     * @param request 上传请求
     * @return 对象信息
     */
    OssObjectResponse upload(OssUploadRequest request);

    /**
     * 下载对象。
     *
     * @param request 对象定位请求
     * @return 对象信息和对象流
     */
    OssObjectResponse download(OssObjectRequest request);

    /**
     * 判断对象是否存在。
     *
     * @param request 对象定位请求
     * @return true 表示对象存在
     */
    boolean exists(OssObjectRequest request);

    /**
     * 删除对象。
     *
     * @param request 对象定位请求
     */
    void delete(OssObjectRequest request);

    /**
     * 查询对象元信息。
     *
     * @param request 对象定位请求
     * @return 对象元信息
     */
    OssObjectResponse getObjectInfo(OssObjectRequest request);

    /**
     * 获取对象访问 URL。
     *
     * @param request 对象定位请求
     * @return 访问 URL
     */
    String getAccessUrl(OssObjectRequest request);
}
