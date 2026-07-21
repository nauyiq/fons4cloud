package com.fons.cloud.auth.oauth.core;

import cn.hutool.core.util.StrUtil;
import com.fons.cloud.auth.domain.entity.Account;
import com.fons.cloud.auth.domain.entity.OauthClient;
import com.fons.cloud.auth.domain.service.AccountDomainService;
import com.fons.cloud.auth.constants.AccountResultCode;
import com.fons.cloud.auth.domain.service.SysOauthClientDomainService;
import com.fons.cloud.auth.security.api.UserDetailsServiceWrapper;
import com.fons.cloud.auth.security.core.SecurityAuthUser;
import com.fons.cloud.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/2/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultUserDetailServiceImpl implements UserDetailsServiceWrapper {
    private final AccountDomainService service;
    private final SysOauthClientDomainService SysOauthClientDomainService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] clientIdAndUsername = username.split(StrUtil.COLON);
        String clientId = clientIdAndUsername[0];
        String name = clientIdAndUsername[1];

        Account account = service.queryAccountByUsernameAndClientId(name, clientId);
        if (Objects.isNull(account)) {
            throw new UsernameNotFoundException(AccountResultCode.ACCOUNT_NOT_FOUND.message);
        }
        OauthClient oauthClient = SysOauthClientDomainService.findByClientId(account.getClientId());
        if (Objects.isNull(oauthClient)) {
            throw new UsernameNotFoundException(AccountResultCode.AUTH_CLIENT_NOT_EXIST.message);
        }

        String authorities = oauthClient.getAuthorities();
        if (!authorities.contains(account.getRole().name())) {
            authorities = StringUtils.isBlank(authorities) ? account.getRole().name() : authorities + "," + account.getRole().name();
        }
        UserDetails userDetails = new SecurityAuthUser(account.getId(), account.getUsername(), account.getPassword(),
                account.getEmail(), account.getPhone(), account.getStatus(), account.getRole(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
        //校验user.
        checkUserDetails(userDetails);
        return userDetails;
    }


    private void checkUserDetails(UserDetails user) {
        if (!user.isEnabled()) {
            log.warn("[{}] -> user status is false.", JsonUtil.toJson(user));
            throw new DisabledException(AccountResultCode.USER_DISABLED.message);
        } else if (!user.isAccountNonLocked()) {
            throw new LockedException("该账号已被锁定!");
        } else if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("该账号已过期!");
        }
    }
}
