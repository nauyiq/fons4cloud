package com.fons.cloud.web.advice;

import com.fons.cloud.common.sensitive.SensitiveDataSanitizer;
import com.fons.cloud.common.sensitive.SensitiveTypeDetector;
import com.fons.cloud.web.config.SensitiveProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * MVC 响应体统一脱敏 Advice，替代原 {@code @BsWebAdvice(desensitize)} 方法级注解开关。
 *
 * <p>在响应体序列化前介入：基于声明返回类型（含 ResponseEntity 外壳与容器泛型）
 * 和运行时载荷做 {@code @Sensitive} 字段检测，命中才脱敏，未命中原样返回（热路径 O(1) 缓存查表）。
 * 下游无需在接口上标注任何注解，由 {@code fons.cloud.sensitive.*} 配置统一控制。</p>
 *
 * <p>边界：SSE/流式响应（Flux/SseEmitter）不经消息转换器路径，本 Advice 不覆盖，需流内自理；
 * 泛型声明为 Object/Map 的载荷无法静态识别，应使用类型化 DTO。</p>
 *
 * @author hongqy
 */
@ControllerAdvice
public class SensitiveResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final SensitiveProperties properties;

    public SensitiveResponseBodyAdvice(SensitiveProperties properties) {
        this.properties = properties;
    }

    /**
     * 总开关短路，关闭时零开销；路径豁免与类型检测在 beforeBodyWrite 中完成。
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return properties.isEnabled();
    }

    /**
     * 检测并脱敏：路径豁免或未命中敏感类型时原样返回，保持原响应类型不变。
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || body instanceof byte[] || body instanceof Resource) {
            return body;
        }
        // 内部接口/导出等需要真实数据的场景按路径豁免
        if (properties.isExcluded(request.getURI().getPath())) {
            return body;
        }
        // 声明类型为 ResponseEntity/HttpEntity 时，处理器的 body 已是剥壳后的实际内容，
        // 但声明类型仍带外壳，这里统一剥掉再进入识别器
        Type declaredType = unwrapHttpEntityType(returnType.getGenericParameterType());
        if (!SensitiveTypeDetector.needSanitize(declaredType, body)) {
            return body;
        }
        return SensitiveDataSanitizer.sanitize(body);
    }

    /**
     * 声明类型为 HttpEntity/ResponseEntity 时取其泛型参数作为实际响应类型。
     */
    private Type unwrapHttpEntityType(Type declaredType) {
        if (declaredType instanceof ParameterizedType parameterized
                && parameterized.getActualTypeArguments().length > 0) {
            Class<?> rawType = (Class<?>) parameterized.getRawType();
            if (HttpEntity.class.isAssignableFrom(rawType)) {
                return parameterized.getActualTypeArguments()[0];
            }
        }
        return declaredType;
    }
}
