# 网关服务能力建模报告

> 更新日期：2026-06-26
> 执行范围：单能力域

## 1. 本次建模范围

| 能力域 | 建模状态 | 风险等级 | 主要证据 | 输出文档 |
| --- | --- | --- | --- | --- |
| 网关服务（gateway-service） | 已完成 | P0 | `fons4cloud-gateway` POM、`application.yml`、动态路由、资源服务器鉴权、全局过滤器、节流风控、Sentinel 响应、验证码源码、授权服务既有文档、用户逐问确认 | `.specify/memory/capabilities/gateway-service/` |

## 2. 处理顺序

| 顺序 | 能力域 | 排序原因 |
| --- | --- | --- |
| 1 | 网关服务 | 已有授权服务建模后，网关侧 Token 内省消费、动态路由和限流风控仍处于待建模状态，且属于 P0 安全入口能力。 |

## 3. 逐问确认结果

| 编号 | 问题 | 用户确认 | 建模影响 |
| --- | --- | --- | --- |
| Q1 | 能力域名称、slug 和主范围 | 按推荐：`网关服务 / gateway-service` | 输出目录确定为 `.specify/memory/capabilities/gateway-service/`，统一覆盖路由、鉴权、限流、验证码等网关模块能力。 |
| Q2 | 职责边界 | 按推荐 | 网关定位为“入口转发 + 鉴权消费 + 流控执行”，不负责 Token 生命周期、权限资源注册和生产规则发布。 |
| Q3 | 核心平台能力清单 | 按推荐 | 主文档覆盖动态路由、服务发现、资源服务器、Token 内省、用户透传、请求包装、节流风控、Sentinel 响应、验证码和异常响应。 |
| Q4 | 公共抽象与标准骨架 | 按推荐 | 将框架契约和项目抽象写成标准骨架；Nacos、Redis、Sentinel、Hutool Captcha 标记为代表性实现。 |
| Q5 | 能力适配对象 | 按推荐 | 矩阵纳入 7 个适配对象：Nacos 动态路由、资源服务器鉴权、用户透传、请求包装、自定义 HTTP 节流、Sentinel 流控、验证码入口。 |
| Q6 | 适配说明和知识卡片 | 按推荐 | 生成 3 个代表性适配说明和 6 张知识卡片；验证码只写入主文档和矩阵。 |

## 4. 生成文件

| 类型 | 文件 |
| --- | --- |
| 能力文档 | `.specify/memory/capabilities/gateway-service/网关服务能力文档.md` |
| 运行文档 | `.specify/memory/capabilities/gateway-service/网关服务运行文档.md` |
| 配置与资源文档 | `.specify/memory/capabilities/gateway-service/网关服务配置与资源文档.md` |
| 适配矩阵 | `.specify/memory/capabilities/gateway-service/能力适配矩阵.md` |
| 适配说明 | `.specify/memory/capabilities/gateway-service/adaptations/nacos-动态路由能力适配说明.md` |
| 适配说明 | `.specify/memory/capabilities/gateway-service/adaptations/resource-server-资源服务器鉴权能力适配说明.md` |
| 适配说明 | `.specify/memory/capabilities/gateway-service/adaptations/http-throttle-HTTP节流风控能力适配说明.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-001 Nacos动态路由刷新.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-002 WebFlux资源服务器鉴权.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-003 AUTH_USER用户信息透传.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-004 请求体重复读取包装.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-005 HTTP节流与黑白名单.md` |
| 知识卡片 | `.specify/memory/capabilities/gateway-service/cards/KC-GW-006 Sentinel网关流控响应.md` |

## 5. 标准判定摘要

| 结论 | 依据 | 说明 |
| --- | --- | --- |
| 动态路由标准骨架是 Gateway `RouteDefinitionRepository` 与项目配置模型。 | Spring Cloud Gateway 契约、`DynamicRouteConfigProperties` | Nacos 是当前代表性实现，不是唯一标准。 |
| 网关鉴权标准骨架是 WebFlux Resource Server、Opaque Token 内省和权限 Facade。 | `ResourceServerConfiguration`、`AuthorizationManager`、授权服务文档 | Token 生命周期仍属于授权服务。 |
| HTTP 节流标准骨架是 `HttpThrottles`、`ThrottlesServer` 和 Gateway 全局过滤器。 | `GatewayHttpThrottles`、`ThrottlesProcess`、`GlobalHttpThrottleFilter` | 具体阈值、开关默认值和生产规则待确认。 |
| Sentinel 当前只写成接入和响应处理，不写成完整已验证执行链路。 | `application.yml`、`SentinelGatewayConfiguration` | 模块内 Sentinel Filter Bean 被注释，实际启用来源待确认。 |

## 6. 待确认问题

| 编号 | 问题 | 影响 | 建议处理 |
| --- | --- | --- | --- |
| GW-RQ-001 | `gateway-routing.json` 的正式路由样例、命名规范、发布流程和回滚方式未确认。 | 动态路由接入治理。 | 后续结合 Nacos 配置快照或运维规范补充。 |
| GW-RQ-002 | Sentinel Gateway Filter 的实际启用来源、规则格式和生产阈值未确认。 | Sentinel 网关流控链路可信度。 | 后续读取自动配置、启动日志或 Nacos 规则。 |
| GW-RQ-003 | 下游服务消费 `AUTH_USER` 的标准 SDK、签名或网络信任边界未确认。 | 网关到下游的认证信任边界。 | 后续建模安全接入或下游解析组件。 |
| GW-RQ-004 | 请求体重复读取包装对大请求体、文件上传和流式请求的治理规则未确认。 | 网关内存稳定性。 | 后续结合生产请求类型和压测策略确认。 |
