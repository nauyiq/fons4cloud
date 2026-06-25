# 项目知识库索引

> 项目名称：fons4cloud
> 项目类型：技术框架/基础设施项目
> 知识状态：基线已建立
> 确认状态：已确认
> 更新日期：2026-06-25

## 1. 项目简介

- 项目定位：fons4cloud 是面向 Java 微服务体系的云基础能力框架，提供认证鉴权、网关、消息队列、公共组件、Starter 接入和运行治理能力。
- 核心技术能力：依赖与版本管理、公共模型与工具、认证鉴权、安全资源扫描、OAuth2/Token 支撑、网关接入与动态路由、网关鉴权与限流、MQ 抽象与 Kafka/RabbitMQ/RocketMQ 适配、缓存与 Redis Stream、数据库接入与分库分表、分布式锁、限流治理、任务调度、分布式事务与 Canal 适配、文件上传与 OSS 适配、可观测性日志链路、Nacos/Dubbo Starter 接入。
- 主要使用方：需要复用云基础能力的 Java 微服务应用、网关运行服务、认证服务和基础组件接入方。
- 当前知识可信度：项目级基线已由用户确认；能力细节来自 Maven 模块、配置文件、AutoConfiguration imports、公共 API 和代表性源码命名，仍需按能力域深度建模。
- 基线确认摘要：项目类型确认为技术框架/基础设施项目；能力域确认为 `云基础能力框架 / cloud-foundation-framework`；本项目不抽象业务领域。

## 2. 快速导航

| 你想了解什么 | 文档路径 | 状态 |
| --- | --- | --- |
| 项目技术能力总览 | `.specify/memory/项目技术能力架构文档.md` | 已生成 |
| 项目运行总览 | `.specify/memory/项目运行架构文档.md` | 已生成 |
| 项目配置与资源总览 | `.specify/memory/项目配置与资源架构文档.md` | 已生成 |
| 技术能力知识 | `.specify/memory/capabilities/{capability-slug}/` | 按能力建模 |
| MQ 抽象与多中间件适配 | `.specify/memory/capabilities/mq-adaptation/` | 已建模 |

## 3. 技术能力域索引

| 技术能力域 | 能力标识 | 职责 | 确认状态 | 能力文档 | 运行文档 | 配置与资源文档 | 建模状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 云基础能力框架 | `cloud-foundation-framework` | 负责 Java 微服务运行所需的基础能力封装与接入约束，包括公共模型与工具、认证鉴权、网关、MQ 抽象与多中间件适配、缓存、数据库接入、分布式锁、限流、任务调度、事务/Canal、文件存储、可观测性、Nacos/Dubbo Starter。 | 已确认 | `.specify/memory/项目技术能力架构文档.md` | `.specify/memory/项目运行架构文档.md` | `.specify/memory/项目配置与资源架构文档.md` | 候选，待技术能力建模 |
| MQ 抽象与多中间件适配 | `mq-adaptation` | 负责消息发送、消息消费抽象、事务消息、本地消息表协作、动态消息资源初始化、Canal 消息监听转发，以及 Kafka/RabbitMQ/RocketMQ 三类中间件适配差异。 | 已确认 | `.specify/memory/capabilities/mq-adaptation/MQ抽象与多中间件适配能力文档.md` | `.specify/memory/capabilities/mq-adaptation/MQ抽象与多中间件适配运行文档.md` | `.specify/memory/capabilities/mq-adaptation/MQ抽象与多中间件适配配置与资源文档.md` | 已建模 |

## 4. 核心平台能力索引

| 平台能力 | 所属技术能力域 | 说明 | 是否存在能力适配风险 | 建模状态 |
| --- | --- | --- | --- | --- |
| 依赖与版本管理 | 云基础能力框架 | 根 POM 统一管理 JDK、Spring Boot、Spring Cloud、Spring Cloud Alibaba、Dubbo 等版本。 | 是 | 待建模 |
| 公共模型与工具 | 云基础能力框架 | `fons4cloud-common-base`、`fons4cloud-common-util` 提供结果模型、请求模型、异常、转换器、并发工具等基础对象。 | 否 | 待建模 |
| 认证鉴权与安全资源扫描 | 云基础能力框架 | `fons4cloud-auth` 提供 auth core、spring security、service API 和认证服务能力。 | 是 | 待建模 |
| OAuth2/Token 支撑 | 云基础能力框架 | 认证模块和网关模块存在 OAuth2、Token、客户端鉴权相关对象。 | 是 | 待建模 |
| 网关接入与动态路由 | 云基础能力框架 | `fons4cloud-gateway` 基于 Spring Cloud Gateway，配置动态路由、服务发现和 Nacos 路由数据。 | 是 | 待建模 |
| 网关鉴权与限流 | 云基础能力框架 | 网关模块包含鉴权过滤器、请求包装过滤器、Sentinel 网关流控配置和限流处理。 | 是 | 待建模 |
| MQ 抽象与多中间件适配 | 云基础能力框架 | `fons4cloud-mq` 覆盖 API、common、Kafka、RabbitMQ、RocketMQ，并提供 Producer、Factory、Message 抽象与实现。 | 是 | 已建模：`.specify/memory/capabilities/mq-adaptation/` |
| 缓存与 Redis Stream | 云基础能力框架 | `fons4cloud-common-cache` 提供缓存自动配置、Redis 操作封装和 Redis Stream 相关支持。 | 是 | 待建模 |
| 数据库接入与分库分表 | 云基础能力框架 | `fons4cloud-common-db` 覆盖 db-core、datasource、shardingsphere。 | 是 | 待建模 |
| 分布式锁 | 云基础能力框架 | `fons4cloud-common-lock` 提供分布式锁注解、切面、Redisson 实现和自动配置。 | 是 | 待建模 |
| 限流治理 | 云基础能力框架 | `fons4cloud-common-limiter` 提供 Sentinel、滑动窗口、令牌桶、IP 黑白名单等限流对象。 | 是 | 待建模 |
| 任务调度 | 云基础能力框架 | `fons4cloud-common-quartz` 与 `fons4cloud-common-xxljob` 提供调度相关配置和自动配置。 | 是 | 待建模 |
| 分布式事务与 Canal 适配 | 云基础能力框架 | `fons4cloud-common-seata`、`fons4cloud-common-canal` 提供事务和 binlog 事件适配能力。 | 是 | 待建模 |
| 文件上传与 OSS 适配 | 云基础能力框架 | `fons4cloud-common-file` 提供文件上传、OSS 存储、Ali/Minio/Tencent 等实现和测试。 | 是 | 待建模 |
| 可观测性日志链路 | 云基础能力框架 | `fons4cloud-common-skywalking` 提供日志配置与链路观测相关依赖。 | 是 | 待建模 |
| Nacos/Dubbo Starter 接入 | 云基础能力框架 | `fons4cloud-starter` 下包含 Nacos 与 Dubbo Starter，Dubbo 侧存在 Filter、Facade 注解和切面。 | 是 | 待建模 |

## 5. 高风险技术能力与建模优先级

| 优先级 | 技术能力域 | 原因 | 建议动作 |
| --- | --- | --- | --- |
| P0 | MQ 抽象与多中间件适配 | 涉及 Kafka、RabbitMQ、RocketMQ 多实现，消息可靠性、事务消息、动态资源初始化和 Canal 监听存在适配风险。 | 优先技术能力建模 |
| P0 | 认证鉴权、OAuth2/Token 与网关鉴权 | 影响安全边界、认证链路、资源扫描、网关过滤器和跨服务调用。 | 优先技术能力建模 |
| P0 | 数据库接入、分库分表与分布式事务 | 影响连接池、事务一致性、ShardingSphere 策略和 Seata 接入。 | 优先技术能力建模 |
| P1 | 限流治理与网关流控 | 影响生产流量治理、Sentinel 配置、IP 黑白名单和限流算法选择。 | 建议专项建模 |
| P1 | 文件上传与 OSS 适配 | 存在多存储供应方适配、上传校验、访问控制和测试证据。 | 建议专项建模 |
| P1 | Nacos/Dubbo Starter 接入 | 影响配置中心、注册发现、RPC 拦截器和外部项目接入方式。 | 建议专项建模 |

## 6. 待确认问题

| 编号 | 类型 | 问题 | 影响范围 | 建议处理 |
| --- | --- | --- | --- | --- |
| Q-001 | 技术能力建模范围 | 是否按 P0/P1 优先级逐个建立能力域深度文档。 | 后续 `.specify/memory/capabilities/` 文档结构。 | 后续使用 `fons4ai-domain-knowledge-modeling` 时确认。 |
| Q-002 | 标准链路细节 | 各能力是否存在团队认可的标准接入步骤、配置模板或生产治理规范。 | 能力级接入链路、初始化链路、运行链路。 | 项目级不展开，能力建模时逐项确认。 |
| Q-003 | 运行环境事实 | 生产环境 Nacos、Redis、MQ、数据库、Sentinel、SkyWalking 等资源归属和部署方式未在项目级确认。 | 配置与资源所有权、运行风险治理。 | 后续结合部署文档或用户确认补充。 |

## 7. 已完成确认项

| 确认项 | 结论 | 确认来源 | 影响范围 |
| --- | --- | --- | --- |
| 项目类型 | 技术框架/基础设施项目 | 用户确认 A | 输出技术能力、运行、配置与资源三类项目级文档。 |
| 项目定位 | fons4cloud 是面向 Java 微服务体系的云基础能力框架，提供认证鉴权、网关、消息队列、公共组件、Starter 接入和运行治理能力。 | 用户确认 A | 作为知识库定位和架构文档总述。 |
| 技术能力域命名 | `云基础能力框架 / cloud-foundation-framework` | 用户按推荐确认 | 作为项目级能力域入口。 |
| 能力域边界 | 负责基础能力封装与接入约束，不负责具体业务系统的业务领域建模、业务流程编排和业务数据标准。 | 用户确认 A | 防止把 auth-service 中的账号对象误建模为项目级业务领域。 |
| 核心平台能力 | 使用已确认的核心平台能力清单。 | 用户确认 A | 作为能力索引和后续建模候选。 |
| 标准链路口径 | 只确认通用链路，不把单个中间件、单个配置样例或单个模块实现写成全项目标准流程。 | 用户确认 A | 控制项目级运行架构的抽象粒度。 |
