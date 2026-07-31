package com.fons.cloud.auth.satoken.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * fons4cloud 独立认证（Sa-Token）扩展配置。
 * <p>
 * Sa-Token 原生配置仍走 {@code sa-token.*} 前缀，本类仅承载 fons4cloud 的扩展项：
 * 全局登录校验开关与拦截/放行路径。
 *
 * @author fons
 */
@Data
@ConfigurationProperties(prefix = "sys.sa-token")
public class FonsSaTokenProperties {

    /** 是否对拦截路径强制执行全局登录校验（StpUtil.checkLogin），默认开启 */
    private boolean globalLoginCheck = true;

    /** 需要拦截的路径，默认拦截全部 */
    private List<String> includePaths = List.of("/**");

    /** 放行路径（如登录、健康检查等），支持 Ant 风格 */
    private List<String> excludePaths = new ArrayList<>();
}
