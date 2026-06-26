# KC-GW-003 AUTH_USER 用户信息透传

> 知识编号：KC-GW-003
> 知识类型：接口契约
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/user
> 可信度说明：来自 `SecurityAuthenticationFilter` 源码和用户确认。
> 关联能力：认证用户透传
> 关联适配：AUTH_USER 用户信息透传适配
> 关联场景：CS-GW-005
> 关联对象：`SecurityAuthenticationFilter`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/filter/SecurityAuthenticationFilter.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关在 Bearer Token 认证成功后，把 `SecurityAuthUser` 转为 `DefaultAuthUser`，序列化为 JSON 后 Base64 编码写入 `AUTH_USER` 请求头。
- 事实粒度：单一接口契约。
- 适用范围：下游服务通过请求头识别认证用户。
- 不适用范围：不确认下游统一解析 SDK、Header 签名和网络信任边界。
- 证据依据：`SecurityAuthenticationFilter`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 防伪造 | 用户透传 | AUTH_USER | 客户端请求已含 `AUTH_USER` 时抛 `AuthException`。 | 已验证 |
| Header 内容 | 用户透传 | AUTH_USER | 包含 id、username、email、phone、userRole、authorities。 | 已验证 |
| 编码格式 | 用户透传 | AUTH_USER | JSON 序列化后 Base64 编码。 | 已验证 |

## 3. 技术落地

- 入口：WebFlux `WebFilter`。
- 应用服务：无。
- 领域对象/方法：`SecurityAuthUser`、`DefaultAuthUser`。
- 仓储/Mapper：无。
- 外部协作：下游服务请求头消费。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/网关服务能力文档.md`
- 数据文档：无。
- 相关卡片：`KC-GW-002 WebFlux 资源服务器鉴权`
