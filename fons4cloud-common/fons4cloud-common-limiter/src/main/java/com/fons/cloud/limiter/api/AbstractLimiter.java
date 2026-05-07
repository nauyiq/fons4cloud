package com.fons.cloud.limiter.api;

import com.fons.cloud.limiter.flow.FlowLimitConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2022/6/17
 */
@Data
@Slf4j
public abstract class AbstractLimiter implements Limiter {

    private FlowLimitConfig config;

    public AbstractLimiter(FlowLimitConfig config) {
        this.config = config;
    }
}
