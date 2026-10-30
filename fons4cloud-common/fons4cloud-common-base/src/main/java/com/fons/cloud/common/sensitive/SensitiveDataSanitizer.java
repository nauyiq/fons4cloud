package com.fons.cloud.common.sensitive;

import cn.hutool.json.JSONUtil;
import com.fons.cloud.common.response.MultiResponse;
import com.fons.cloud.common.response.SingleResponse;
import com.fons.cloud.common.result.R;
import com.github.houbb.sensitive.core.api.SensitiveUtil;

import java.util.List;

/**
 * 敏感数据脱敏执行器，web/dubbo 共用，合并自原 BsWebAdviceAspect 与 FacadeAspect 的重复逻辑。
 *
 * <p>按响应容器类型分派：R/SingleResponse 只脱敏 data 字段，MultiResponse 脱敏列表元素，
 * JSON 字符串走 desJson（返回值仍为字符串，保持消息转换器类型一致），
 * 其余对象整体 desCopy。容器引用本身不变，仅替换其中的敏感数据。</p>
 *
 * <p>调用方需先经 {@link SensitiveTypeDetector#needSanitize} 判定命中后再执行，
 * 避免对不含敏感字段的响应产生反射拷贝开销。</p>
 *
 * @author hongqy
 */
public final class SensitiveDataSanitizer {

    private SensitiveDataSanitizer() {
    }

    /**
     * 对响应体执行脱敏。
     *
     * @param body 响应体，支持 R/SingleResponse/MultiResponse/JSON 字符串/普通对象
     * @return 脱敏后的响应体，类型与入参一致
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object sanitize(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof R) {
            R r = (R) body;
            r.setData(SensitiveUtil.desCopy(r.getData()));
            return r;
        }
        if (body instanceof SingleResponse) {
            SingleResponse response = (SingleResponse) body;
            response.setData(SensitiveUtil.desCopy(response.getData()));
            return response;
        }
        if (body instanceof MultiResponse) {
            MultiResponse multipleResponse = (MultiResponse) body;
            multipleResponse.setData((List) SensitiveUtil.desCopyCollection(multipleResponse.getData()));
            return multipleResponse;
        }
        if (body instanceof String text) {
            // 非 JSON 字符串不处理，避免误改普通文本响应
            return JSONUtil.isTypeJSON(text) || JSONUtil.isTypeJSONArray(text) ? SensitiveUtil.desJson(text) : text;
        }
        return SensitiveUtil.desCopy(body);
    }
}
