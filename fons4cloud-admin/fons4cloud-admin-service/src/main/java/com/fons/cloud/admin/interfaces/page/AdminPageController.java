package com.fons.cloud.admin.interfaces.page;

import com.fons.cloud.web.annotation.BsWebAdvice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Vue 控制台页面入口。
 *
 * <p>只对已知 SPA 路由执行 index fallback，带 hash 的 assets 仍由 Spring 静态资源处理器直接提供。</p>
 */
@Controller
@RequestMapping("/admin-ui")
public class AdminPageController {

    private static final String SPA_INDEX = "forward:/admin-ui/index.html";

    /**
     * 支持应用入口与所有能力工作区深路由刷新。
     *
     * @return Vue 生产构建的 index.html
     */
    @GetMapping({
            "", "/", "/login", "/overview",
            "/services", "/services/**",
            "/gateway", "/gateway/**",
            "/traffic", "/traffic/**",
            "/access", "/access/**",
            "/clients", "/clients/**",
            "/changes", "/changes/**",
            "/audits", "/audits/**"
    })
    @BsWebAdvice(requiredToken = false)
    public String spa() {
        return SPA_INDEX;
    }
}
