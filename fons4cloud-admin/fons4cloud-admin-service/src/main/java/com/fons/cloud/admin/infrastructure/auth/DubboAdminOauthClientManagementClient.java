package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientModifyRequest;
import com.fons.cloud.auth.request.OauthClientQueryRequest;
import com.fons.cloud.auth.request.OauthClientRotateSecretRequest;
import com.fons.cloud.auth.request.OauthClientStatusRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.auth.response.OauthClientSecretRotateResult;
import com.fons.cloud.auth.service.OauthClientManagementFacadeService;
import com.fons.cloud.common.result.R;
import com.fons.cloud.dubbo.DubboConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Dubbo 的 OAuth Client 管理 RPC 客户端。
 */
@Component
public class DubboAdminOauthClientManagementClient implements AdminOauthClientManagementClient {

    @DubboReference(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)
    private OauthClientManagementFacadeService oauthClientManagementFacadeService;

    @Override
    public R<OauthClientInfo> query(OauthClientQueryRequest request) {
        return oauthClientManagementFacadeService.query(request);
    }

    @Override
    public R<List<OauthClientInfo>> queryList(OauthClientQueryRequest request) {
        return oauthClientManagementFacadeService.queryList(request);
    }

    @Override
    public R<OauthClientSecretRotateResult> create(OauthClientCreateRequest request) {
        return oauthClientManagementFacadeService.create(request);
    }

    @Override
    public R<OauthClientInfo> update(OauthClientModifyRequest request) {
        return oauthClientManagementFacadeService.update(request);
    }

    @Override
    public R<Boolean> updateStatus(OauthClientStatusRequest request) {
        return oauthClientManagementFacadeService.updateStatus(request);
    }

    @Override
    public R<OauthClientSecretRotateResult> rotateSecret(OauthClientRotateSecretRequest request) {
        return oauthClientManagementFacadeService.rotateSecret(request);
    }
}
