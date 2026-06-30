package com.fons.cloud.infrastructure.facade;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import com.fons.cloud.cache.random.RandomCodeFacade;
import com.fons.cloud.cache.random.RandomCodeScene;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.common.result.R;
import com.fons.cloud.common.result.ResultCode;
import com.fons.cloud.infrastructure.constants.InfrastructureResultCode;
import com.fons.cloud.infrastructure.request.SendAuthSmsRequest;
import com.fons.cloud.infrastructure.service.CommunicationFacadeService;
import com.fons.cloud.infrastructure.sms.strategt.SmsStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.concurrent.TimeUnit;

import static com.fons.cloud.dubbo.DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION;

/**
 * @author hongqy
 */
@Slf4j
@RequiredArgsConstructor
@DubboService(version = DEFAULT_DUBBO_SERVICE_VERSION)
public class CommunicationFacadeServiceImpl implements CommunicationFacadeService {
    private final SmsStrategy smsStrategy;
    private final RandomCodeFacade randomCodeFacade;


    @Override
    public R<Boolean> sendAuthSms(SendAuthSmsRequest request) {
        String phone = request.getPhone();
        Assert.isTrue(Validator.isMobile(phone), () -> new BizException(InfrastructureResultCode.INCORRECT_PHONE));
        // 发送验证码
        String code = randomCodeFacade.randomNumber(
                6,
                request.getExpiredSeconds() == null ? 600 : request.getExpiredSeconds(),
                TimeUnit.SECONDS,
                RandomCodeScene.SMS_AUTH,
                request.getClientId(),
                phone);

        return smsStrategy.sendCode(request.getClientId(), phone, code) ? R.ok() : R.failed(InfrastructureResultCode.FAILED_SEND_SMS);
    }
}
