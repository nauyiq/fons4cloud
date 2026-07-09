package com.fons.cloud.auth.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fons.cloud.auth.domain.entity.OauthClient;

/**
 * @author qiyuan.hong
 * @date 2022-03-16 14:52
 */
public interface SysOauthClientDomainService extends IService<OauthClient> {

    /**
     * 根据租户id获取租户信息
     * @param clientId 租户id
     * @return          租户信息
     */
    OauthClient findByClientId(String clientId);

    /**
     * 清理客户端缓存。
     * @param clientId 客户端ID
     */
    void evictClientCache(String clientId);

}
