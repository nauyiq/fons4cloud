package com.fons.cloud.gateway.server;

import com.fons.cloud.common.request.HttpRequestInfo;
import com.fons.cloud.common.swticher.ServerSwitcher;
import com.fons.cloud.limiter.api.HttpThrottles;
import com.fons.cloud.limiter.core.LimitResult;
import com.fons.cloud.limiter.flow.FlowResult;
import com.fons.cloud.limiter.flow.HttpAccessFlowControlCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.fons.cloud.common.base.lang.StringConstants.Symbol.INCLINED_ROD;

/**
 * Http限流器，内部实现了系统忙或者客户端频繁访问时，判定要否限流的功能。也能识别出基本的hack或者数据采集，继而判定要限制访问。<br>
 * 核心实现依赖 redis zset的滑动窗口算法和 google的RateLimiter的平滑突发限流 令牌桶算法
 * @author qy
 * @date 2021-07-27 19:58
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayHttpThrottles implements HttpThrottles {

    private final HttpAccessFlowControlCenter flowControlCenter;
    private final ThrottlesProcess throttlesProcess;
    private static final int MAX_LENGTH = 1024;

    @Override
    public LimitResult limitValue(HttpRequestInfo request) {
        String uri = request.getUri();
        String requestIp = request.getRequestIp();
        String requestUrl = request.getRequestUrl();
        String requestParams = request.getRequestParams();
        String url = StringUtils.isBlank(requestUrl) ? uri : requestUrl;

        // 是否是人工指定的黑名单阻塞的ip
        if (throttlesProcess.isManualBlockedIp(requestIp)) {
            return new LimitResult(true, printErrorMessage(requestIp, url, "[MBK]"), LimitResult.ReasonEnum.MANUAL_BLOCKED_IP_NG);
        }
        // 是否是行为分析的黑名单ip
        if (throttlesProcess.isBiBlockedIp(requestIp)) {
            return new LimitResult(true, printErrorMessage(requestIp, url, "[BBK]"), LimitResult.ReasonEnum.BI_BLOCKED_IP_NG);
        }
        //是否校验请求中的xss 聚合浓缩黑客判定方法
        if (ServerSwitcher.ENABLE_HTTP_THROTTLE_SECURITY_CHECKING.isOn()) {
            LimitResult hackCheckLimitResult = checkHackAccess(uri, url, requestParams, request.getRequestBody(), requestIp);
            if (hackCheckLimitResult.isNeedLimit()) {
                hackCheckLimitResult.setTip(printErrorMessage(requestIp, url, "[HAK]"));
                return hackCheckLimitResult;
            }
        }
        if (ServerSwitcher.ENABLE_HTTP_THROTTLE_VALVE.isOff()) {
            return new LimitResult(false, null, LimitResult.ReasonEnum.NOT_ENABLE_HTTP_THROTTLE_OK);
        } else {
            //检查ip是否访问超限
            try {
                FlowResult flowResult = flowControlCenter.needLimitPerTimeWindow(requestIp, request.getMethod(), uri);
                boolean needLimit = flowResult.isOverLimit();
                if (flowResult.isBlock()) {
                    throttlesProcess.addBiBlockIp(requestIp, flowResult.getBlockSeconds());
                }
                if (needLimit) {
                    return new LimitResult(true, printErrorMessage(requestIp, url, ""), LimitResult.ReasonEnum.RATE_LIMIT_NG);
                }
            } catch (Exception e) {
                log.error("Throttles HTTP request [requestIp=" + requestIp + "] failed", e);
            }

        }
        return new LimitResult();
    }

    private String printErrorMessage(String requestIp, String url, String mode) {
        return "Too many requests from [requestIp=" + requestIp + ", url=" + url + "] " + mode;
    }


    @Override
    public LimitResult checkHackAccess(String uri, String url, String QueryString, String requestBody, String requestIp) {
        //聚合浓缩黑客判定方法....
        if (ServerSwitcher.ENABLE_HTTP_THROTTLE_SECURITY_CHECKING.isOn()) {
            //检查请求体
            if (StringUtils.isNotBlank(requestBody)) {
                if (throttlesProcess.isHackAccess(requestBody, ThrottlesProcess.PARAMS_CHECK_MODE)) {
                    return limitHackAccessAndPersistBlockIp(requestIp);
                }
            }
            //检查url或请求参数
            if (StringUtils.isNotBlank(QueryString)) {
                if (throttlesProcess.isHackAccess(QueryString, ThrottlesProcess.URI_CHECK_MODE)) {
                    return limitHackAccessAndPersistBlockIp(requestIp);
                }
            }
            //检查uri
            if (StringUtils.isNotBlank(uri) && !INCLINED_ROD.equals(uri)) {
                if (throttlesProcess.isHackAccess(uri, ThrottlesProcess.URI_CHECK_MODE)) {
                    return limitHackAccessAndPersistBlockIp(requestIp);
                }
            }
        }
        return new LimitResult();
    }

    private LimitResult limitHackAccessAndPersistBlockIp(String requestIp) {
        // 纳入黑名单，访问限制!!!!
        throttlesProcess.addManualBlockIp(requestIp, ThrottlesProcess.IP_ACCESS_BLOCK_SECONDS);
        return new LimitResult(true, LimitResult.ReasonEnum.HACK_TOOL_ACCESS_NG);
    }

    @Override
    public boolean isWhiteURI(String uri) {
        return throttlesProcess.isWhiteUri(uri);
    }


    @Override
    public boolean isManualWhiteIp(String remoteAddr) {
        return throttlesProcess.isWhiteIp(remoteAddr);
    }

}
