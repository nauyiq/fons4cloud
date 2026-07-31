# KC-AS-003 路由拦截与白名单

> 知识编号：KC-AS-003
> 知识类型：技术流程
> 所属领域：独立认证（authentication-standalone）
> 状态：已验证
> 来源：code/config/test
> 可信度说明：基于 `SaTokenWebMvcConfigurer` 源码与 `FonsSaTokenProperties` 默认值
> 关联能力：独立认证
> 关联适配：sa-token
> 关联场景：CS-AS-003（路由拦截强制登录校验）
> 关联对象：SaTokenWebMvcConfigurer、FonsSaTokenProperties、SaInterceptor
> 关联代码：`fons4cloud-auth-satoken/.../config/SaTokenWebMvcConfigurer.java`、`FonsSaTokenProperties.java`
> 更新日期：2026-07-31

## 1. 事实描述

- 核心事实：`SaTokenWebMvcConfigurer` 实现 `WebMvcConfigurer`，注册 `SaInterceptor` 拦截 `includePaths`（默认 `/**`）并排除 `excludePaths`；当 `globalLoginCheck=true`（默认）时对拦截路径强制调用 `StpUtil.checkLogin()`，未登录抛 `NotLoginException`。
- 事实粒度：单一技术流程（路由拦截与白名单）
- 适用范围：WebMVC 环境的独立认证
- 不适用范围：WebFlux 响应式环境
- 证据依据：`SaTokenWebMvcConfigurer.java`（`addInterceptors` 方法注册 `SaInterceptor` + lambda 调用 `StpUtil.checkLogin`）、`FonsSaTokenProperties.java`（默认值 `globalLoginCheck=true`、`includePaths=["/**"]`、`excludePaths=[]`）

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 默认拦截全部 | 独立认证 | sa-token | `includePaths` 默认 `["/**"]` | 已验证 |
| 全局登录校验 | 独立认证 | sa-token | `globalLoginCheck=true` 时拦截路径强制 `StpUtil.checkLogin` | 已验证 |
| 白名单放行 | 独立认证 | sa-token | `excludePaths` 支持 Ant 风格，登录接口必须配置放行 | 已验证 |
| 注解鉴权叠加 | 独立认证 | sa-token | `SaInterceptor` 默认开启注解鉴权（`@SaCheckPermission`/`@SaCheckRole`） | 已验证 |

## 3. 技术落地

- 入口：`SaTokenWebMvcConfigurer.addInterceptors`（由 `FonsSaTokenAutoConfiguration` 装配，`@ConditionalOnWebApplication`）
- 应用服务：`SaInterceptor`（sa-token 提供）
- 领域对象/方法：`FonsSaTokenProperties.isGlobalLoginCheck()`、`getIncludePaths()`、`getExcludePaths()`
- 仓储/Mapper：无
- 外部协作：无
- 测试：`FonsSaTokenPropertiesTest`（默认值与自定义绑定）、`SaTokenAuthTemplateTest`（`globalLoginCheck=false` 放行测试端点）
