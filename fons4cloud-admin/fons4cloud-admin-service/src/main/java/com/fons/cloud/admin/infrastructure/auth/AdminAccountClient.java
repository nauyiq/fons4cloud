package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.common.result.R;

/**
 * admin 查询认证服务账号信息的 RPC 客户端端口。
 */
public interface AdminAccountClient {

    /**
     * 根据认证账号 ID 查询账号信息。
     *
     * @param accountId 认证服务账号 ID
     * @return 账号信息
     */
    R<AccountInfo> queryById(Long accountId);

    /**
     * 根据用户名查询账号信息。
     *
     * @param username 用户名
     * @return 账号信息
     */
    R<AccountInfo> queryByUsername(String username);
}
