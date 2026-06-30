package com.fons.cloud.cache.random;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fons.cloud.cache.common.RedisException;
import com.fons.cloud.cache.redis.server.support.SmartRedisManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/7/2
 */
@Slf4j
public class RedisRandomCodeFacade implements RandomCodeFacade {

    @Override
    public String randomNumber(int length, int expired, TimeUnit timeUnit, RandomCodeScene scene, String tag, String value) {
        Assert.isTrue(length > 0, "length must be greater than 0");
        // 生成随机的code
        String code = RandomUtil.randomNumbers(length);
        return doRandom(expired, timeUnit, scene, tag, code, value);
    }

    @Override
    public String randomStr(int length, int expired, TimeUnit timeUnit, RandomCodeScene scene, String tag, String value) {
        Assert.isTrue(length > 0, "length must be greater than 0");
        // 生成随机的code
        String code = RandomUtil.randomString(length);
        return doRandom(expired, timeUnit, scene, code, tag, value);
    }

    private String doRandom(long expired, TimeUnit timeUnit, RandomCodeScene scene, String tag, String code, String value) {
        String redisKey = getRedisKey(code, tag, scene);
        Boolean set = SmartRedisManager.getInstance().set(redisKey, value, expired, timeUnit);
        if (Boolean.TRUE.equals(set)) {
            return code;
        }
        log.error("Failed execute to set redisKey:{}", redisKey);
        throw new RedisException("Failed execute to set redisKey:" + redisKey);
    }

    @Override
    public boolean isExist(String code, String value, String tag, RandomCodeScene scene) {
        String redisKey = getRedisKey(code, tag, scene);
        String data = SmartRedisManager.getInstance().get(redisKey);
        return StringUtils.isNotBlank(data) && data.equals(value);
    }

    private String getRedisKey(String code, String tag, RandomCodeScene scene){
        if (StringUtils.isBlank(tag)) {
            return scene.suffix + code;
        }
        return  scene.suffix + tag + StrUtil.COLON + code;
    }
    


}
