# KC-GW-006 Sentinel 网关流控响应

> 知识编号：KC-GW-006
> 知识类型：治理规则
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/config/user
> 可信度说明：配置和 BlockHandler 已验证，Sentinel Gateway Filter 实际装配来源待确认。
> 关联能力：Sentinel 网关流控
> 关联适配：Sentinel 网关流控接入适配
> 关联场景：CS-GW-008
> 关联对象：`SentinelGatewayConfiguration`、`SentinelExceptionHandler`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/config/SentinelGatewayConfiguration.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关配置了 Sentinel Gateway Nacos flow 数据源，并在启动后通过 `GatewayCallbackManager.setBlockHandler` 注册 `SentinelExceptionHandler`，block 时返回统一 JSON 响应。
- 事实粒度：单一治理规则。
- 适用范围：Sentinel Gateway 触发 block 的响应处理。
- 不适用范围：不确认 Sentinel Gateway Filter 的实际装配来源和生产规则阈值。
- 证据依据：`application.yml`、`SentinelGatewayConfiguration`、`SentinelExceptionHandler`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 规则数据源 | Sentinel 流控 | Sentinel Gateway | Nacos dataId 为 `gateway-sentinel-flow`，ruleType 为 `flow`。 | 已验证配置 |
| BlockHandler | Sentinel 流控 | Sentinel Gateway | 注册 `SentinelExceptionHandler`。 | 已验证 |
| 响应内容 | Sentinel 流控 | Sentinel Gateway | 返回 `R.failed(ResultCode.INTERFACE_BUSY_LIMIT)`，HTTP 403。 | 已验证 |
| Filter Bean | Sentinel 流控 | Sentinel Gateway | 模块内 `SentinelGatewayFilter` Bean 代码被注释。 | 待确认 |

## 3. 技术落地

- 入口：Sentinel Gateway block 回调。
- 应用服务：无。
- 领域对象/方法：`BlockRequestHandler.handleRequest`。
- 仓储/Mapper：无。
- 外部协作：Sentinel Gateway、Nacos flow 规则。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/网关服务运行文档.md`
- 数据文档：`.specify/memory/capabilities/gateway-service/网关服务配置与资源文档.md`
- 相关卡片：无。
