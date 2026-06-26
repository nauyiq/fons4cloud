# KC-GW-001 Nacos 动态路由刷新

> 知识编号：KC-GW-001
> 知识类型：能力适配
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/config/user
> 可信度说明：来自网关配置、Nacos 路由仓库源码和用户确认。
> 关联能力：动态路由
> 关联适配：Nacos 动态路由适配
> 关联场景：CS-GW-001、CS-GW-002
> 关联对象：`NacosRouteDefinitionRepository`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/nacos/NacosRouteDefinitionRepository.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关通过 `NacosRouteDefinitionRepository` 从 Nacos `gateway-routing.json` 读取 `RouteDefinition`，并在配置变化时发布 `RefreshRoutesEvent`。
- 事实粒度：单一能力适配。
- 适用范围：`gateway-service` 动态路由配置源为 Nacos 的场景。
- 不适用范围：不说明路由配置发布流程、路由 JSON 字段规范和路由灰度策略。
- 证据依据：`application.yml`、`DynamicRouteConfiguration`、`NacosRouteDefinitionRepository`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 路由读取 | 动态路由 | Nacos | 调用 Nacos `getConfig` 并转换为 `RouteDefinition` 列表。 | 已验证 |
| 路由刷新 | 动态路由 | Nacos | Nacos Listener 收到配置变化后发布 `RefreshRoutesEvent`。 | 已验证 |
| 路由写入 | 动态路由 | Nacos | `save/delete` 当前未实现，不能写成网关写路由能力。 | 已验证 |

## 3. 技术落地

- 入口：Spring Cloud Gateway 读取 `RouteDefinitionRepository`。
- 应用服务：无。
- 领域对象/方法：`getRouteDefinitions()`、`addListener()`。
- 仓储/Mapper：无。
- 外部协作：Nacos ConfigService。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/网关服务能力文档.md`
- 数据文档：`.specify/memory/capabilities/gateway-service/网关服务配置与资源文档.md`
- 相关卡片：无。
