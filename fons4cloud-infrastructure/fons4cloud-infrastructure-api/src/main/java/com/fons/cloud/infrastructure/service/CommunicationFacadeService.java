package com.fons.cloud.infrastructure.service;

import com.fons.cloud.common.result.R;
import com.fons.cloud.infrastructure.request.SendAuthSmsRequest;

/**
 * 通信Facade服务
 * @author hongqy
 */
public interface CommunicationFacadeService {

    /**
     * 发送验证码短信
     * @param request 入参
     * @return
     */
    R<Boolean> sendAuthSms(SendAuthSmsRequest request);

}
