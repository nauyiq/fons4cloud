package com.fons.cloud.limiter.core;

import com.fons.cloud.common.base.project.ProjectContextInfo;
import com.fons.cloud.limiter.api.DefaultManualWhiteIpAdaptor;
import org.redisson.api.RedissonClient;

/**
 * ManualWhiteIpService.
 * @see DefaultManualWhiteIpAdaptor
 * @author qy
 * @date 2021/9/14 23:30
 */
public class ManualWhiteIpRedisService extends DefaultManualWhiteIpAdaptor {

    public ManualWhiteIpRedisService(RedissonClient redisson) {
        super(ProjectContextInfo.WHITE_IP_PROPERTIES_KEY, redisson);
        initializeWhiteIp(false);
    }


}
