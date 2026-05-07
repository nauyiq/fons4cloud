package com.fons.cloud.dubbo.facade;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fons.cloud.common.response.MultiResponse;
import com.fons.cloud.common.response.SingleResponse;
import com.fons.cloud.common.result.R;
import com.github.houbb.sensitive.core.api.SensitiveUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Facade 切面处理类， 统一进行参数校验和异常捕获，采集
 *
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/7/9
 */
@Slf4j
@Aspect
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class FacadeAspect {

    @Around("@annotation(com.fons.cloud.dubbo.facade.Facade)")
    public Object facade(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Object[] args = pjp.getArgs();
        if (log.isDebugEnabled()) {
            log.debug("start to execute , method = {} , args = {}", method.getName(), JSON.toJSONString(args));
        }

        Facade facade = method.getAnnotation(Facade.class);

        //循环遍历所有参数，进行参数校验
        Object response = pjp.proceed();

        if (facade.desensitize()) {
            // 如果需要脱敏展示数据， 则脱敏后返回
            return desensitize(response);
        }
        return response;
    }

    /**
     * 脱敏处理, 基于sensitive
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object desensitize(Object result) {
        if (result instanceof R<?>) {
            R r = (R) result;
            Object desCopyData = SensitiveUtil.desCopy(r.getData());
            r.setData(desCopyData);
            return r;
        } else if (result instanceof SingleResponse) {
            SingleResponse response = (SingleResponse) result;
            Object desCopyData = SensitiveUtil.desCopy(response.getData());
            response.setData(desCopyData);
            return response;
        } else if (result instanceof MultiResponse) {
            MultiResponse multipleResponse = (MultiResponse) result;
            List desCopyCollection = SensitiveUtil.desCopyCollection(multipleResponse.getData());
            multipleResponse.setData(desCopyCollection);
            return multipleResponse;
        } else if (result instanceof String && (JSONUtil.isTypeJSON((String) result) || JSONUtil.isTypeJSONArray((String) result))) {
            return SensitiveUtil.desJson(result);
        } else {
            return SensitiveUtil.desCopy(result);
        }
    }


}
