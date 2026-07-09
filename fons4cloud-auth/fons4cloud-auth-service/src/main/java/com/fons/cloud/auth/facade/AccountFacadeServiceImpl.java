package com.fons.cloud.auth.facade;

import cn.hutool.core.lang.Assert;
import com.fons.cloud.auth.constants.AccountResultCode;
import com.fons.cloud.auth.constants.AccountStatus;
import com.fons.cloud.auth.constants.GrantType;
import com.fons.cloud.auth.request.AccountAuthRequest;
import com.fons.cloud.auth.request.AccountModifyRequest;
import com.fons.cloud.auth.request.AccountQueryParams;
import com.fons.cloud.auth.request.AuthenticateRequest;
import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.auth.response.TokenInfo;
import com.fons.cloud.auth.service.AccountFacadeService;
import com.fons.cloud.auth.domain.entity.Account;
import com.fons.cloud.auth.domain.service.AccountDomainService;
import com.fons.cloud.auth.application.AccountApplicationService;
import com.fons.cloud.auth.application.AuthenticationApplicationService;
import com.fons.cloud.auth.infrastructure.converter.AccountConverter;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.common.result.R;
import com.fons.cloud.common.result.ResultCode;
import com.fons.cloud.dubbo.DubboConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/7/11
 */
@Slf4j
@RequiredArgsConstructor
@DubboService(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)
public class AccountFacadeServiceImpl implements AccountFacadeService {
    private final PasswordEncoder passwordEncoder;
    private final AccountDomainService accountDomainService;
    private final AccountApplicationService accountApplicationService;
    private final AuthenticationApplicationService authenticationApplicationService;

    @Override
    public R<AccountInfo> query(AccountQueryParams queryParams) {
        Account account = null;
        Long id = queryParams.getId();
        if (id != null) {
            account = accountDomainService.findById(id);
        } else if (StringUtils.isNotBlank(queryParams.getUsername())) {
            account = accountDomainService.queryAccountByUniqueIndex(queryParams.getUsername());
        } else if (StringUtils.isNotBlank(queryParams.getPhone())) {
            account = accountDomainService.queryAccountByUniqueIndex(queryParams.getPhone());
        } else if (StringUtils.isNotBlank(queryParams.getEmail())) {
            account = accountDomainService.queryAccountByUniqueIndex(queryParams.getEmail());
        }
        if (account == null) {
            return R.failed(AccountResultCode.ACCOUNT_NOT_FOUND);
        }

        AccountInfo accountInfo = AccountConverter.CONVERTER.mapToVo(account);
        return R.ok(accountInfo);
    }

    @Override
    public R<List<AccountInfo>> queryList(Collection<Long> ids) {
        List<Account> accounts = accountDomainService.listByIds(ids);
        return R.ok(accounts.stream().map(AccountConverter.CONVERTER::mapToVo).toList());
    }

    @Override
    public R<TokenInfo> registerAndAuthenticate(AuthenticateRequest request) {
        // 1. 请求校验, 注册并认证只支持SMS/EMAIL模式
        GrantType grantType = request.getGrantType();
        if (grantType != GrantType.EMAIL && grantType != GrantType.SMS) {
            return R.failed(AccountResultCode.UNSUPPORTED_AUTHENTICATION_GRANT_TYPE);
        }

        // 2. 注册
        Account account = accountApplicationService.register(request);
        log.info("账号注册入库成功: id:{}, username:{}", account.getId(), account.getUsername());

        // 3. 认证
        return R.ok(authenticationApplicationService.authenticate(request));
    }

    @Override
    public R<AccountInfo> realNameAuth(AccountAuthRequest request) {
        Account account = accountDomainService.findById(request.getId());
        Assert.notNull(account, () -> new BizException(AccountResultCode.ACCOUNT_NOT_FOUND));
        if (account.getCertification()) {
            // 已经实名
            return R.ok(AccountConverter.CONVERTER.mapToVo(account));
        }
        if (account.getStatus() == AccountStatus.DISABLED) {
            throw new BizException(AccountResultCode.USER_DISABLED);
        }

        account.auth(request.getRearName(), request.getIdCard());
        boolean auth = accountApplicationService.realNameAuth(account);
        return auth ? R.ok(AccountConverter.CONVERTER.mapToVo(account) ): R.failed(AccountResultCode.USER_AUTH_FAIL);
    }

    @Override
    public R<Boolean> updatePassword(AccountModifyRequest request) {
        Account account = accountDomainService.findById(request.getId());
        if (account == null) {
            return R.failed(AccountResultCode.ACCOUNT_NOT_FOUND);
        }
        String password = account.getPassword();
        if (!passwordEncoder.matches(request.getOldPassword(), password)) {
            return R.failed(AccountResultCode.INCORRECT_PASSWORD);
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return accountDomainService.updateById(account) ? R.ok() : R.failed(ResultCode.UPDATE_FAILED);
    }


}
