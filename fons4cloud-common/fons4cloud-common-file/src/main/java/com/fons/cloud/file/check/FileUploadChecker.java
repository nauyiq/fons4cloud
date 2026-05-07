package com.fons.cloud.file.check;

import com.fons.cloud.file.common.FileException;
import com.fons.cloud.file.common.request.BaseFileUploadRequest;

/**
 * 文件上传校验器
 * @author hongqy
 * @date 2025/5/6
 */
public interface FileUploadChecker {

    /**
     * 文件上传检查
     * @param baseFileUploadRequest 上传请求
     * @throws FileException    通用文件异常
     */
    void check(BaseFileUploadRequest baseFileUploadRequest) throws FileException;

}
