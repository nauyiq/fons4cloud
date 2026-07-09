package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.auth.request.AccountQueryParams;
import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.auth.service.AccountFacadeService;
import com.fons.cloud.common.result.R;
import com.fons.cloud.dubbo.DubboConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 基于 Dubbo 的认证账号查询客户端。
 */
@Component
public class DubboAdminAccountClient implements AdminAccountClient {

    @DubboReference(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)
    private AccountFacadeService accountFacadeService;

    @Override
    public R<AccountInfo> queryById(Long accountId) {
        AccountQueryParams queryParams = new AccountQueryParams();
        queryParams.setId(accountId);
        return accountFacadeService.query(queryParams);
    }

    @Override
    public R<AccountInfo> queryByUsername(String username) {
        AccountQueryParams queryParams = new AccountQueryParams();
        queryParams.setUsername(username);
        return accountFacadeService.query(queryParams);
    }
}
