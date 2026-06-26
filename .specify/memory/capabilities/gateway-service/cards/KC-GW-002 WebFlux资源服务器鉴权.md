# KC-GW-002 WebFlux 资源服务器鉴权

> 知识编号：KC-GW-002
> 知识类型：技术流程
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/docs/user
> 可信度说明：来自网关安全配置、授权管理器源码和授权服务能力文档。
> 关联能力：网关鉴权
> 关联适配：资源服务器鉴权适配
> 关联场景：CS-GW-003、CS-GW-004
> 关联对象：`ResourceServerConfiguration`、`AuthorizationManager`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/config/ResourceServerConfiguration.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关作为 WebFlux 资源服务器，使用 Opaque Token 内省识别 Bearer Token，并通过 `AuthPermissionService.isPermitRequest` 判断请求权限。
- 事实粒度：单一技术流程。
- 适用范围：非白名单 HTTP 请求的网关鉴权。
- 不适用范围：Token 生成、刷新、吊销和权限资源注册。
- 证据依据：`ResourceServerConfiguration`、`AuthorizationManager`、授权服务能力文档。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 白名单 | 网关鉴权 | 资源服务器鉴权 | OPTIONS、静态端点和业务白名单 URI 放行。 | 已验证 |
| Token 内省 | 网关鉴权 | 资源服务器鉴权 | 通过 `DefaultReactiveOpaqueTokenIntrospector` 查询 Redis Token。 | 已验证 |
| 权限判定 | 网关鉴权 | 资源服务器鉴权 | 构造 `AuthenticationRequest` 并调用授权服务权限 Facade。 | 已验证 |

## 3. 技术落地

- 入口：`SecurityWebFilterChain`。
- 应用服务：`AuthorizationManager`。
- 领域对象/方法：`AuthPermissionService.isPermitRequest`。
- 仓储/Mapper：无。
- 外部协作：授权服务 Redis Token 存储、权限资源。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/网关服务运行文档.md`
- 数据文档：`.specify/memory/capabilities/authorization-service/授权服务运行文档.md`
- 相关卡片：`KC-GW-003 AUTH_USER 用户信息透传`
