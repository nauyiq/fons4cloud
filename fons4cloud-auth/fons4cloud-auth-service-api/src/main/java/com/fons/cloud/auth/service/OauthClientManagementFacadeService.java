package com.fons.cloud.auth.service;

import com.fons.cloud.auth.request.*;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.common.result.R;

import java.util.List;

/**
 * OAuth Client 管理 RPC 契约。
 * admin 必须通过该契约治理客户端，不能跨库直写 oauth_client。
 */
public interface OauthClientManagementFacadeService {

    /**
     * 按客户端ID查询单个 OAuth Client 脱敏视图。
     *
     * @param request 查询条件，clientId 必填
     * @return OAuth Client 脱敏信息，不包含 clientSecret
     */
    R<OauthClientInfo> query(OauthClientQueryRequest request);

    /**
     * 查询 OAuth Client 列表。
     *
     * @param request 可选查询条件，支持按 clientId 和启停状态过滤
     * @return OAuth Client 脱敏信息列表
     */
    R<List<OauthClientInfo>> queryList(OauthClientQueryRequest request);

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求；clientSecret 为空时由认证服务生成
     * @return 一次性密钥结果，明文密钥只允许通过本次响应返回
     */
    R<OauthClientSecretRotateResult> create(OauthClientCreateRequest request);

    /**
     * 修改 OAuth Client 基础信息，不允许修改 clientSecret。
     *
     * @param request 修改请求，使用 version 做乐观锁控制
     * @return 修改后的脱敏信息
     */
    R<OauthClientInfo> update(OauthClientModifyRequest request);

    /**
     * 启用或禁用 OAuth Client。
     *
     * @param request 启停请求，使用 version 做乐观锁控制
     * @return 是否操作成功
     */
    R<Boolean> updateStatus(OauthClientStatusRequest request);

    /**
     * 轮换 OAuth Client 密钥。
     *
     * @param request 轮换请求；newClientSecret 为空时由认证服务生成
     * @return 一次性密钥结果，明文密钥只允许通过本次响应返回
     */
    R<OauthClientSecretRotateResult> rotateSecret(OauthClientRotateSecretRequest request);

    /**
     * 清理指定 OAuth Client 缓存。
     *
     * @param clientId 客户端ID
     * @return 是否操作成功
     */
    R<Boolean> evictCache(String clientId);
}
