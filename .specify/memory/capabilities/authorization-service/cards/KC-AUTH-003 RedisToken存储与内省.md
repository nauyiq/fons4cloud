# KC-AUTH-003 Redis Token 存储与内省

> 知识编号：KC-AUTH-003
> 知识类型：能力适配
> 所属能力：授权服务（authorization-service）
> 状态：已验证
> 来源：code
> 可信度说明：来自 Redis OAuth2AuthorizationService 和 Opaque Token Introspector 源码。
> 关联能力：Token 生命周期
> 关联适配：Redis Token 存储、Opaque Token 内省
> 关联场景：CS-AUTH-006、CS-AUTH-008
> 关联对象：`RedisOAuth2AuthorizationService`、`DefaultReactiveOpaqueTokenIntrospector`
> 关联代码/接口/SQL：`fons4cloud-auth/fons4cloud-auth-spring-security/src/main/java/com/fons/cloud/auth/security/core/`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：授权服务用 Redis 实现 `OAuth2AuthorizationService`，按 state/code/access_token/refresh_token 保存授权对象；资源服务或网关可通过 Opaque Token Introspector 查询 access token 并得到 principal。
- 事实粒度：单一能力适配。
- 适用范围：Token 保存、刷新、吊销、校验和内省。
- 不适用范围：不定义 Redis 部署拓扑、监控告警和容灾策略。
- 证据依据：`RedisOAuth2AuthorizationService.java`、`DefaultReactiveOpaqueTokenIntrospector.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| Key 格式 | Token 生命周期 | Redis Token 存储 | `token::{type}::{id}`。 | 已验证 |
| TTL | Token 生命周期 | Redis Token 存储 | state 固定 10 分钟，code/access/refresh 按自身有效期。 | 已验证 |
| 内省 | Token 生命周期 | Opaque Token 内省 | access token 无效时抛 invalid bearer token。 | 已验证 |
| client_credentials | Token 生命周期 | Opaque Token 内省 | 返回无 authorities 的 client principal。 | 已验证 |

## 3. 技术落地

- 入口：`OAuth2AuthorizationService.save/findByToken/remove`、`DefaultReactiveOpaqueTokenIntrospector.introspect`
- 应用服务：授权服务、资源服务、网关
- 领域对象/方法：不适用
- 仓储/Mapper：RedisTemplate
- 外部协作：Redis
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 适配说明：`../adaptations/redis-token存储能力适配说明.md`
- 配置与资源文档：`../授权服务配置与资源文档.md`
