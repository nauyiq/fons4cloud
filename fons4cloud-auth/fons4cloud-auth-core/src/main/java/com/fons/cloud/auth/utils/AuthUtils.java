package com.fons.cloud.auth.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fons.cloud.auth.api.AuthUser;
import com.fons.cloud.auth.api.support.DefaultAuthUser;
import com.fons.cloud.auth.common.AuthException;
import com.fons.cloud.auth.common.AuthUserHeaderConstants;
import com.fons.cloud.auth.common.UserRole;
import com.fons.cloud.auth.common.UsernamePasswordAuthentication;
import com.fons.cloud.common.result.ResultCode;
import com.fons.cloud.util.AssertUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.fons.cloud.common.base.lang.AuthConstants.*;

/**
 * 认证授权工具类
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/6/25
 */
@Slf4j
@UtilityClass
public class AuthUtils {

    private static final ThreadLocal<AuthUser> THREAD_LOCAL = new InheritableThreadLocal<>();

    public void removeUser() {
        THREAD_LOCAL.remove();
    }

    public AuthUser getCurrentUser() {
        AuthUser authUser = THREAD_LOCAL.get();
        if (authUser != null) {
            return authUser;
        }

        HttpServletRequest request = WebUtils.currentRequest();
        Assert.notNull(request, "Current env not support spring mvc.");
        // 从请求头获取用户信息
        try {
            String authUserJson = request.getHeader(AuthUserHeaderConstants.AUTH_USER);
            AssertUtil.notEmpty(authUserJson, "AuthUser is empty from request header.");
            authUser = JSON.parseObject(Base64.decodeStr(authUserJson), DefaultAuthUser.class);
        } catch (Exception cause) {
            log.error(cause.getMessage(), cause);
            throw new AuthException(ResultCode.INVALID_ACCESS_TOKEN);
        }
        if (authUser != null) {
            THREAD_LOCAL.set(authUser);
        } else {
            throw new AuthException(ResultCode.INVALID_ACCESS_TOKEN);
        }

       return authUser;
    }


    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserName() {
        return getCurrentUser().getUsername();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public String getCurrentUserPhone() {
       return getCurrentUser().getPhone();
    }

    public UserRole getCurrentUserRole() {
      return getCurrentUser().getUserRole();
    }

    public List<String> getCurrentAuthorities() {
        return getCurrentUser().authorities();
    }


    public boolean checkAuthorization(String authorization) {
        return authorization.startsWith(JWT_PREFIX) || authorization.startsWith(JWT_UPPERCASE_PREFIX) || authorization.startsWith(JWT_BASIC_PREFIX);
    }

    /**
     * 获取basic认证请求头
     * @param authorization 认证请求头
     * @return              {@link UsernamePasswordAuthentication}
     */
    public UsernamePasswordAuthentication getBasicAuthorization(String authorization) {
        if (!isBasicAuthorization(authorization)) {
            return null;
        }
        String basic = authorization.replace(JWT_BASIC_PREFIX, org.apache.commons.lang3.StringUtils.EMPTY).replace(JWT_LOWERCASE_BASIC_PREFIX, org.apache.commons.lang3.StringUtils.EMPTY);
        if (org.apache.commons.lang3.StringUtils.isBlank(basic)) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(basic);
            String decodeBasic = new String(bytes);
            String[] basics = decodeBasic.split(StrUtil.COLON);
            if (basics.length != 2) {
                return null;
            }
            return new UsernamePasswordAuthentication(basics[0], basics[1]);
        } catch (Throwable cause) {
            log.error("Failed execute to parse basic authentication: {}.", basic, cause);
            return null;
        }
    }

    /**
     * 判断认证请求头是不是basic认证请求头
     * @param authorization 认证请求头
     * @return              是否是basic认证
     */
    public boolean isBasicAuthorization(String authorization) {
        if (org.apache.commons.lang3.StringUtils.isBlank(authorization)) {
            return false;
        }
        return authorization.startsWith(JWT_BASIC_PREFIX) || authorization.startsWith(JWT_LOWERCASE_BASIC_PREFIX);
    }

    /**
     * 生成basic认证
     * @param username 用户名
     * @param password 密码
     * @return         basic认证.
     */
    public static String buildBasicAuth(String username, String password) {
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(username, password)) {
            return null;
        }
        String auth = Base64.encode((username.concat(StrUtil.COLON).concat(password)).getBytes(StandardCharsets.UTF_8));
        return JWT_BASIC_PREFIX + auth;
    }

}
