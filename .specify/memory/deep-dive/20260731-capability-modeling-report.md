# 领域知识建模报告

> 更新日期：2026-07-31
> 执行范围：单能力域

## 1. 本次建模范围

| 领域 | 建模状态 | 风险等级 | 主要证据 | 输出文档 |
| --- | --- | --- | --- | --- |
| 独立认证（authentication-standalone） | 已完成 | P0 | 本次新增 `fons4cloud-auth-satoken` 模块源码、pom 依赖、自动配置注册、单元测试 7 个全部通过、common-cache 调研结论、authorization-service 现有文档边界对照 | `.specify/memory/capabilities/authentication-standalone/` |

## 2. 处理顺序

| 顺序 | 领域 | 排序原因 |
| --- | --- | --- |
| 1 | 独立认证 | 用户明确要求对认证模块深度建模并包含本次 sa-token 改动；sa-token 是新增的独立认证能力，需与现有 authorization-service 边界对照 |

## 3. 未处理领域

| 领域 | 原因 | 建议动作 |
| --- | --- | --- |
| 无 | 本次仅建模独立认证一个能力域 | 后续可按 P0/P1 优先级处理 index.md 中其他待建模能力域 |

## 4. 需要用户确认的问题

| 编号 | 领域 | 问题 | 影响 | 建议处理 |
| --- | --- | --- | --- | --- |
| Q-001 | 独立认证 | 建模对象归属：纳入现有 authorization-service 还是新建独立能力域 | 文档路径与边界 | 已确认：新建独立能力域 |
| Q-002 | 独立认证 | 能力域中文名与 slug | 索引与文档命名 | 已确认：独立认证 / authentication-standalone |
| Q-003 | 独立认证 | 能力边界划定 | 主文档职责章节 | 已确认：仅会话与鉴权接入 |
| Q-004 | 独立认证 | 是否生成适配矩阵及适配划分 | 文档结构 | 已确认：生成矩阵，当前 1 适配 |
| Q-005 | 独立认证 | 公共抽象与标准能力判定 | 主文档标准判定章节 | 已确认：三抽象为标准 |

## 5. 生成文件清单

| 文件 | 类型 | 状态 |
| --- | --- | --- |
| `capabilities/authentication-standalone/独立认证能力文档.md` | 能力文档 | 已生成 |
| `capabilities/authentication-standalone/独立认证运行文档.md` | 运行文档 | 已生成 |
| `capabilities/authentication-standalone/独立认证配置与资源文档.md` | 配置与资源文档 | 已生成 |
| `capabilities/authentication-standalone/能力适配矩阵.md` | 适配矩阵 | 已生成 |
| `capabilities/authentication-standalone/cards/KC-AS-001 独立认证公共骨架.md` | 知识卡片 | 已生成 |
| `capabilities/authentication-standalone/cards/KC-AS-002 Sa-Token会话与Redis持久化.md` | 知识卡片 | 已生成 |
| `capabilities/authentication-standalone/cards/KC-AS-003 路由拦截与白名单.md` | 知识卡片 | 已生成 |
| `capabilities/authentication-standalone/cards/KC-AS-004 权限角色接入契约.md` | 知识卡片 | 已生成 |

## 6. 关键证据摘要

| 证据类型 | 内容 |
| --- | --- |
| 源码 | `fons4cloud-auth-satoken` 下 5 个 Java 文件（`SaTokenAuthTemplate`、`FonsSaTokenProperties`、`SaTokenWebMvcConfigurer`、`DefaultStpInterfaceImpl`、`FonsSaTokenAutoConfiguration`） |
| 配置 | `pom.xml`（依赖 sa-token-spring-boot3-starter 1.45.0 + sa-token-redis-jackson + common-cache）、`AutoConfiguration.imports` |
| 测试 | 7 个单元测试全部通过（FonsSaTokenPropertiesTest 2 + DefaultStpInterfaceImplTest 2 + SaTokenAuthTemplateTest 3） |
| 交叉校验 | common-cache 调研结论（Redisson 3.24.3 + RedisConnectionFactory 自动装配）、authorization-service 现有文档边界对照 |
| 用户确认 | 5 个阻塞问题逐个确认（归属、名称、边界、矩阵、标准判定） |

## 7. 标准判定结论

| 结论 | 依据 |
| --- | --- |
| `SaTokenAuthTemplate` 是独立认证标准工具入口 | 封装登录/会话/踢人，业务方注入即用 |
| `SaTokenWebMvcConfigurer`+`FonsSaTokenProperties` 是标准路由拦截配置骨架 | 注册 SaInterceptor + 白名单 |
| `StpInterface` 是权限/角色接入标准契约 | Sa-Token 框架契约 |
| Sa-Token 1.45.0 SDK 是特定适配，不作为唯一标准 | 当前唯一实现，无横向对比 |
| sa-token-redis-jackson、Header+Cookie、DefaultStpInterfaceImpl 均为特定适配 | 代表性实现 |

## 8. 待确认事项

| 编号 | 问题 | 影响 | 建议处理 |
| --- | --- | --- | --- |
| CQ-AS-001 | 生产 Redis 实例归属与连接配置未确认 | 会话持久化可靠性 | 后续结合部署文档确认 |
| CQ-AS-002 | Sa-Token 原生配置生产值未确认 | 令牌有效期、多端登录、安全策略 | 后续结合安全规范确认 |
| CQ-AS-003 | 业务方权限/角色数据来源未确认 | 注解鉴权可用性 | 由具体业务应用确认 |
| CQ-AS-004 | 未来是否扩展 JWT/SSO/OAuth2 客户端适配 | 适配矩阵扩展 | 后续按需求触发 |
