package com.fons.cloud.dubbo.filter;

import com.alibaba.fastjson2.JSON;
import com.fons.cloud.common.exception.BsException;
import com.fons.cloud.common.response.Response;
import com.fons.cloud.common.result.ResultCode;
import com.fons.cloud.common.sensitive.SensitiveDataSanitizer;
import com.fons.cloud.common.sensitive.SensitiveTypeDetector;
import com.fons.cloud.util.BeanValidator;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AppResponse;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.fons.cloud.dubbo.DubboConstants.SENSITIVE_DESENSITIZE_PARAMETER;

/**
 * Dubbo Provider 侧统一门面过滤器：参数校验、响应补全、按需脱敏与统一日志。
 *
 * <p>参数校验失败或方法执行异常时，返回类型为 {@link Response} 子类则构造统一格式的失败响应；
 * 日志按统一格式输出便于监控统计（调整格式需同步调整日志监控）。</p>
 *
 * <p>响应脱敏默认关闭：dubbo 响应多为服务间机器消费（发短信、风控、对账需要真实数据），
 * 默认脱敏会静默破坏下游业务；通过 provider URL 参数显式开启，如
 * {@code @DubboService(parameters = {"fonsSensitive", "true"})} 或全局
 * {@code <dubbo:provider parameter="fonsSensitive=true"/>}。开启后基于方法返回类型与运行时
 * 结果做 {@code @Sensitive} 字段检测，命中才脱敏。脱敏固定在日志打印之前执行，
 * 保证日志记录的也是脱敏后的数据。</p>
 *
 * @author hongqy
 * @date 2026/1/30
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class SysFacadeFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 获取执行的方法参数
        Object[] arguments = invocation.getArguments();
        // 获取响应类型：returnType 供失败响应构造，genericReturnType 供脱敏载荷识别
        Class<?> returnType;
        Type genericReturnType;
        try {
            Class<?> invokerInterface = invoker.getInterface();
            Method method = invokerInterface.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            returnType = method.getReturnType();
            genericReturnType = method.getGenericReturnType();
        } catch (NoSuchMethodException e) {
            throw new RpcException("No such method: " + invocation.getMethodName(), e);
        }

        //循环遍历所有参数，进行参数校验
        for (Object parameter : arguments) {
            try {
                BeanValidator.validateObject(parameter);
            } catch (ValidationException e) {
                printLog(stopWatch, invocation.getMethodName(), arguments, "Failed to validate", null, e);
                return getFailedResponse(returnType, e, true);
            }
        }

        try {
            // 目标方法执行
            Result result = invoker.invoke(invocation);
            // 补全响应消息
            enrichObject(result.getValue());
            // 按需脱敏：在日志之前执行，日志记录脱敏后的数据
            desensitizeIfNeeded(invoker, invocation, genericReturnType, result);
            // 打印日志
            printLog(stopWatch, invocation.getMethodName(), arguments, "Rpc execute to", result.getValue(), null);

            return result;
        } catch (Exception e) {
            // 如果执行异常，则返回一个失败的response
            printLog(stopWatch, invocation.getMethodName(), arguments, "Failed to execute", null, e);
            return getFailedResponse(returnType, e, false);
        }

    }

    /**
     * 按需脱敏：仅当 provider 通过 {@code fonsSensitive} 参数开启时执行，默认关闭。
     * <p>脱敏失败只记录日志，不影响调用结果；异常结果不参与脱敏。</p>
     *
     * @param invoker           服务调用器，读取 provider URL 参数
     * @param invocation        当前调用
     * @param genericReturnType 服务方法声明的返回类型（含泛型）
     * @param result            调用结果，脱敏后原地替换 value
     */
    private void desensitizeIfNeeded(Invoker<?> invoker, Invocation invocation, Type genericReturnType, Result result) {
        if (result == null || result.hasException()) {
            return;
        }
        Object value = result.getValue();
        if (value == null || !desensitizeEnabled(invoker)) {
            return;
        }
        try {
            if (!SensitiveTypeDetector.needSanitize(genericReturnType, value)) {
                return;
            }
            Object sanitized = SensitiveDataSanitizer.sanitize(value);
            if (sanitized != value && result instanceof AppResponse response) {
                response.setValue(sanitized);
            }
        } catch (Throwable cause) {
            log.warn("Dubbo 响应脱敏失败, service={}, method={}",
                    invoker.getInterface().getName(), invocation.getMethodName(), cause);
        }
    }

    /**
     * 判断 provider 是否通过 URL 参数开启响应脱敏。
     *
     * @param invoker 服务调用器
     * @return        开启返回 true，默认关闭
     */
    private boolean desensitizeEnabled(Invoker<?> invoker) {
        return Boolean.parseBoolean(invoker.getUrl().getParameter(SENSITIVE_DESENSITIZE_PARAMETER, "false"));
    }

    /**
     * 日志打印
     *
     * @param stopWatch
     * @param method
     * @param args
     * @param action
     * @param response
     */
    private void printLog(StopWatch stopWatch, String method, Object[] args, String action, Object response,
                          Throwable throwable) {
        try {
            //因为此处有JSON.toJSONString，可能会有异常，需要进行捕获，避免影响主干流程
            log.info(getInfoMessage(action, stopWatch, method, args, response, throwable), throwable);
            // 如果校验失败，则返回一个失败的response
        } catch (Exception e1) {
            log.error("log failed", e1);
        }
    }

    /**
     * 统一格式输出，方便做日志统计
     * <p>
     * *** 如果调整此处的格式，需要同步调整日志监控 ***
     *
     * @param action    行为
     * @param method    方法
     * @param args      参数
     * @param response  响应
     * @return 拼接后的字符串
     */
    private String getInfoMessage(String action, StopWatch stopWatch, String method, Object[] args, Object response,
                                  Throwable exception) {

        StringBuilder stringBuilder = new StringBuilder(action);
        stringBuilder.append(", method = ");
        stringBuilder.append(method);
        stringBuilder.append(", cost = ");
        stringBuilder.append(stopWatch.getTime()).append(" ms");
        if (response instanceof Response) {
            stringBuilder.append(", success = ");
            stringBuilder.append(((Response) response).isSuccess());
        }
        if (exception != null) {
            stringBuilder.append(", success = ");
            stringBuilder.append(false);
        }
        stringBuilder.append(", args = ");
        stringBuilder.append(JSON.toJSONString(Arrays.toString(args)));

        if (response != null) {
            stringBuilder.append(", resp = ");
            stringBuilder.append(JSON.toJSONString(response));
        }

        if (exception != null) {
            stringBuilder.append(", exception = ");
            stringBuilder.append(exception.getMessage());
        }

        if (response instanceof Response baseResponse) {
            if (!baseResponse.isSuccess()) {
                stringBuilder.append(", execute_failed");
            }
        }

        return stringBuilder.toString();
    }


    /**
     * 定义并返回一个通用的失败响应
     */
    private Result getFailedResponse(Class returnType, Throwable throwable, boolean valid)
            throws RpcException {
        //如果返回值的类型为BaseResponse 的子类，则创建一个通用的失败响应
        try {
            if (returnType.getDeclaredConstructor().newInstance() instanceof Response response) {
                response.setSuccess(false);
                if (valid) {
                    // 校验入参异常
                    response.setCode(ResultCode.PARAMS_ERROR.getCode());
                    response.setMessage(ResultCode.PARAMS_ERROR.getMessage());
                } else if (throwable instanceof BsException bizException) {
                    response.setCode(bizException.getExceptionCode().getCode());
                    response.setMessage(bizException.getMessage());
                } else {
                    response.setMessage(ResultCode.SYSTEM_INTERVAL_ERROR.getMessage());
                    response.setCode(ResultCode.SYSTEM_INTERVAL_ERROR.getCode());
                }
                return new AppResponse(response);
            }
        } catch (Exception e) {
            throw new RpcException(e);
        }
        log.error("failed to getFailedResponse , returnType ({}) is not instanceof BaseResponse", returnType);
        return null;
    }

    /**
     * 将response的信息补全，主要是code和message
     * @param response
     */
    private void enrichObject(Object response) {
        if (response instanceof Response) {
            if (!((Response) response).isSuccess()) {
                //如果状态是成功的，需要将未设置的responseCode设置成BIZ_ERROR
                if (StringUtils.isEmpty(((Response) response).getMessage())) {
                    ((Response) response).setMessage(ResultCode.SYSTEM_INTERVAL_ERROR.getMessage());
                }
            }
        }
    }

}
