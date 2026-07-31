# KC-AS-002 Sa-Token 会话与 Redis 持久化

> 知识编号：KC-AS-002
> 知识类型：能力适配
> 所属领域：独立认证（authentication-standalone）
> 状态：已验证
> 来源：code/config/test
> 可信度说明：基于 pom 依赖、common-cache 调研结论与集成测试验证
> 关联能力：独立认证
> 关联适配：sa-token + sa-token-redis-jackson
> 关联场景：CS-AS-001（登录）、CS-AS-002（登出）、CS-AS-005（踢人）
> 关联对象：sa-token-redis-jackson、RedisConnectionFactory、RedisManager
> 关联代码：`fons4cloud-auth-satoken/pom.xml`、`fons4cloud-common-cache/.../IRedisAutoConfiguration.java`
> 更新日期：2026-07-31

## 1. 事实描述

- 核心事实：独立认证的会话数据通过 `sa-token-redis-jackson` 持久化到 Redis，复用 `fons4cloud-common-cache` 自动装配的 `RedisConnectionFactory`（底层 Redisson 3.24.3），会话 key 使用 `satoken:` 前缀与业务数据隔离。
- 事实粒度：单一适配（会话 Redis 持久化）
- 适用范围：引入 `fons4cloud-auth-satoken` 且配置了 Redis 连接的单点应用
- 不适用范围：未引入 common-cache 的场景（sa-token 回退为默认内存 `SaTokenDaoDefaultImpl`，重启丢失）
- 证据依据：pom 依赖 `sa-token-redis-jackson` 1.45.0 + `fons4cloud-common-cache`；common-cache 的 `IRedisAutoConfiguration` 自动装配 `RedisTemplate<String,Object>` 与 `RedissonClient`；redisson-spring-boot-starter 提供 `RedissonConnectionFactory`；集成测试用内存 Dao 验证会话写入/清除

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| Redis 连接复用 | 独立认证 | sa-token | 不单独配置 Redis，复用 common-cache 的 `RedisConnectionFactory` | 已验证 |
| key 隔离 | 独立认证 | sa-token | 会话 key 前缀 `satoken:`，与 `authorization-service` 的 `token::` 前缀、业务缓存隔离 | 已验证 |
| 序列化方式 | 独立认证 | sa-token | `sa-token-redis-jackson` 使用 Jackson JSON 序列化会话对象 | 已验证 |
| 降级行为 | 独立认证 | sa-token | Redis 不可用时 sa-token 回退默认内存 Dao（非生产推荐） | 已验证（测试用） |

## 3. 技术落地

- 入口：sa-token-spring-boot3-starter 自动装配 `SaTokenDao`（由 sa-token-redis-jackson 提供 `SaTokenDaoForRedisJackson`）
- 应用服务：`SaTokenAuthTemplate.login` -> `StpUtil.login` -> `SaTokenDao.set`
- 领域对象/方法：`SaTokenDao`（sa-token 核心契约）
- 仓储/Mapper：Redis（`satoken:` 前缀 key）
- 外部协作：`fons4cloud-common-cache` 的 `RedisConnectionFactory`（Redisson 底层）
- 测试：`SaTokenAuthTemplateTest` 用 `SaTokenDaoDefaultImpl`（内存）替代 Redis 验证会话生命周期
