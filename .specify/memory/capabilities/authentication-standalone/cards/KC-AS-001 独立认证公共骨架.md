# KC-AS-001 独立认证公共骨架

> 知识编号：KC-AS-001
> 知识类型：技术流程
> 所属领域：独立认证（authentication-standalone）
> 状态：已验证
> 来源：code/config/test
> 可信度说明：基于本次新增模块源码、pom 依赖与单元测试，7 个测试全部通过
> 关联能力：独立认证
> 关联适配：Sa-Token 1.45.0 SDK 适配
> 关联场景：CS-AS-001 ~ CS-AS-006
> 关联对象：SaTokenAuthTemplate、FonsSaTokenAutoConfiguration
> 关联代码：`fons4cloud-auth/fons4cloud-auth-satoken/src/main/java/com/fons/cloud/auth/satoken/`
> 更新日期：2026-07-31

## 1. 事实描述

- 核心事实：独立认证能力域通过 `FonsSaTokenAutoConfiguration` 自动装配三个 Bean（`SaTokenAuthTemplate`、`DefaultStpInterfaceImpl`、`SaTokenWebMvcConfigurer`），为单点业务应用提供登录/登出/会话校验/踢人/令牌查询的工具入口与路由拦截配置。
- 事实粒度：单一技术流程（自动装配与公共骨架）
- 适用范围：引入 `fons4cloud-auth-satoken` 依赖的 Spring Boot WebMVC 单点应用
- 不适用范围：OAuth2 体系（`authorization-service`）、网关内省、Dubbo RPC 认证
- 证据依据：`FonsSaTokenAutoConfiguration.java`（`@AutoConfiguration` + `@ConditionalOnClass` + `@EnableConfigurationProperties`）、`AutoConfiguration.imports` 注册、`SaTokenAuthTemplate.java`、单元测试 7 个全部通过

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 自动装配条件 | 独立认证 | sa-token | `@ConditionalOnClass(name="cn.dev33.satoken.stp.StpUtil")` + `@ConditionalOnWebApplication` | 已验证 |
| Bean 覆盖 | 独立认证 | sa-token | `SaTokenAuthTemplate`、`StpInterface`、`SaTokenWebMvcConfigurer` 均使用 `@ConditionalOnMissingBean`，业务方可覆盖 | 已验证 |
| 依赖隔离 | 独立认证 | sa-token | 不依赖 auth-core/auth-service-api/common-web/nacos/dubbo，仅依赖 common-cache + sa-token + spring-boot-web | 已验证 |

## 3. 技术落地

- 入口：`FonsSaTokenAutoConfiguration`（注册于 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`）
- 应用服务：`SaTokenAuthTemplate`（业务方注入调用）
- 领域对象/方法：`login`/`logout`/`isLogin`/`checkLogin`/`getCurrentLoginId`/`getTokenValue`/`kickout`
- 仓储/Mapper：无（会话由 sa-token-redis-jackson 持久化）
- 外部协作：Redis（经 common-cache 的 `RedisConnectionFactory`）
- 测试：`FonsSaTokenPropertiesTest`（2）、`DefaultStpInterfaceImplTest`（2）、`SaTokenAuthTemplateTest`（3 集成测试）
