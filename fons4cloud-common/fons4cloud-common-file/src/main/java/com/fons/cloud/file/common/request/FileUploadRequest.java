package com.fons.cloud.file.common.request;

import com.fons.cloud.file.common.constants.FileAccessControl;
import lombok.*;

import java.io.InputStream;

/**
 * @author hongqy
 * @date 2025/5/6
 */
@Getter
@Setter
@ToString(exclude = "inputStream")
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    private String filename;
    private String path;
    private InputStream inputStream;
    private FileAccessControl fileAccessControl;


}
