package com.fons.cloud.web.handler;

import cn.hutool.json.JSONUtil;
import com.fons.cloud.common.base.lang.exception.NotAuthenticationException;
import com.fons.cloud.common.exception.BsException;
import com.fons.cloud.common.exception.ExceptionCode;
import com.fons.cloud.common.exception.ExceptionType;
import com.fons.cloud.common.result.R;
import com.fons.cloud.common.result.ResultCode;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Spring mvc全局异常处理
 * @author hongqy
 */
@Slf4j
@RestControllerAdvice
public class WebMvcGlobalExceptionHandler {


    /**
     * 认证未通过异常处理
     * @param exception
     * @param request
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotAuthenticationException.class)
    public R<Boolean> handle(NotAuthenticationException exception, HttpServletRequest request) {
        return R.failed(ResultCode.INVALID_ACCESS_TOKEN);
    }

    /**
     * 业务异常处理
     * @param e
     * @param request
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BsException.class)
    public R<Boolean> handler(BsException e, HttpServletRequest request) {
        ExceptionCode exceptionCode = e.getExceptionCode();
        ExceptionType type = exceptionCode.exceptionType();
        if (type == ExceptionType.SYSTEM) {
            log.error("[SYSTEM-EXCEPTION][{}][{}][{}]", exceptionCode.module(), exceptionCode.getCode(), e.getMessage(), e);
        } else {
            log.warn("[BS-EXCEPTION][{}][{}][{}]", exceptionCode.module(), exceptionCode.getCode(), e.getMessage(), e);
        }
        return R.setResult(false,  exceptionCode.getCode(), e.getMessage(), null);
    }

    /**
     * 全局业务异常处理， 内部异常
     * @param e
     * @param request
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public R<Boolean> handler(RuntimeException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        return R.failed(ResultCode.SYSTEM_INTERVAL_ERROR);
    }


    /**
     * bean校验未通过异常
     * @param e MethodArgumentNotValidException
     * @return MessageResponse
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Boolean> handler(MethodArgumentNotValidException e) {
        Map<String, String> errors = Maps.newHashMapWithExpectedSize(1);
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return R.failed(JSONUtil.toJsonStr(errors), ResultCode.PARAMS_ERROR.getCode());
    }

    /**
     * 请求体不可读异常处理
     * <p>请求体JSON解析失败、枚举或类型绑定错误属于客户端参数错误，返回400与通用参数错误信息；
     * 解析细节只记录日志不回传客户端，避免内部信息外泄，也避免落入RuntimeException兜底被记为系统故障。</p>
     * @param e    请求体不可读异常
     * @return     参数错误响应
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Boolean> handler(HttpMessageNotReadableException e) {
        log.warn("[HTTP-MESSAGE-NOT-READABLE][{}]", e.getMessage());
        return R.failed(ResultCode.PARAMS_ERROR);
    }

}
