package com.fons.cloud.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 响应脱敏配置。
 *
 * <p>web 响应面向前端展示，默认开启脱敏；内部接口、数据导出等需要真实数据的场景
 * 通过 {@code excludePaths} 按路径豁免。dubbo 侧脱敏不在此配置范围内，
 * 由 provider 参数（fonsSensitive）显式开启。</p>
 *
 * @author hongqy
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sys.sensitive")
public class SensitiveProperties {

    /** Ant 路径匹配器，线程安全，静态复用 */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 响应脱敏总开关，默认开启 */
    private boolean enabled = true;

    /** 豁免路径（Ant 风格，如 /internal/**），命中的响应不脱敏 */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * 判断请求路径是否命中豁免规则。
     *
     * @param path 请求路径
     * @return 命中返回 true
     */
    public boolean isExcluded(String path) {
        for (String pattern : excludePaths) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
