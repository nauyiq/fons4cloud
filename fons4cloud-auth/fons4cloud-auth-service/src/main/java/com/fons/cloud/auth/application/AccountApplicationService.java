package com.fons.cloud.auth.application;

import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.domain.entity.Account;

/**
 * 账号db操作相关service
 * @author qiyuan.hong
 * @version 1.0
 * @date 2022/9/27
 */
public interface AccountApplicationService {

    /**
     * 注册账号
     * @param request 注册参数
     * @return        注册信息
     */
    Account register(AuthenticateRequest request);

    /**
     * 实名认证
     * @param request
     * @return
     */
    boolean realNameAuth(Account request);

}
