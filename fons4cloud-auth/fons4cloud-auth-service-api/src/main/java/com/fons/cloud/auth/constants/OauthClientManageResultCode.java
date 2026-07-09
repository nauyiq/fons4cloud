package com.fons.cloud.auth.constants;

import com.fons.cloud.common.result.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth Client 管理 RPC 状态码。
 */
@Getter
@AllArgsConstructor
public enum OauthClientManageResultCode implements Result {

    OAUTH_CLIENT_NOT_FOUND("OC200001", "OAuth客户端不存在"),
    OAUTH_CLIENT_ALREADY_EXISTS("OC200002", "OAuth客户端已存在"),
    OAUTH_CLIENT_SAVE_FAILED("OC300001", "OAuth客户端保存失败"),
    OAUTH_CLIENT_UPDATE_FAILED("OC300002", "OAuth客户端更新失败"),
    OAUTH_CLIENT_SECRET_ROTATE_FAILED("OC300003", "OAuth客户端密钥轮换失败");

    public final String code;

    public final String message;
}
