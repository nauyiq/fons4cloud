package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientModifyRequest;
import com.fons.cloud.auth.request.OauthClientQueryRequest;
import com.fons.cloud.auth.request.OauthClientRotateSecretRequest;
import com.fons.cloud.auth.request.OauthClientStatusRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.common.result.R;

/**
 * admin 调用认证服务 OAuth Client 管理能力的 RPC 客户端端口。
 */
public interface AdminOauthClientManagementClient {

    /**
     * 查询 OAuth Client 脱敏信息。
     *
     * @param request 查询条件
     * @return OAuth Client 脱敏信息
     */
    R<OauthClientInfo> query(OauthClientQueryRequest request);

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return 一次性密钥结果
     */
    R<OauthClientSecretRotateResult> create(OauthClientCreateRequest request);

    /**
     * 修改 OAuth Client 基础信息。
     *
     * @param request 修改请求
     * @return 修改后的脱敏信息
     */
    R<OauthClientInfo> update(OauthClientModifyRequest request);

    /**
     * 启用或禁用 OAuth Client。
     *
     * @param request 启停请求
     * @return 是否成功
     */
    R<Boolean> updateStatus(OauthClientStatusRequest request);

    /**
     * 轮换 OAuth Client 密钥。
     *
     * @param request 轮换请求
     * @return 一次性密钥结果
     */
    R<OauthClientSecretRotateResult> rotateSecret(OauthClientRotateSecretRequest request);
}
