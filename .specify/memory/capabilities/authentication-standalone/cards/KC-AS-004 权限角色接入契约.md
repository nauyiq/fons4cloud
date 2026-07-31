# KC-AS-004 权限角色接入契约

> 知识编号：KC-AS-004
> 知识类型：接口契约
> 所属领域：独立认证（authentication-standalone）
> 状态：已验证
> 来源：code/test
> 可信度说明：基于 `DefaultStpInterfaceImpl` 源码与 sa-token 框架契约
> 关联能力：独立认证
> 关联适配：sa-token
> 关联场景：CS-AS-004（注解式权限/角色校验）
> 关联对象：StpInterface、DefaultStpInterfaceImpl、@SaCheckPermission、@SaCheckRole
> 关联代码：`fons4cloud-auth-satoken/.../api/DefaultStpInterfaceImpl.java`、`FonsSaTokenAutoConfiguration.java`
> 更新日期：2026-07-31

## 1. 事实描述

- 核心事实：独立认证能力域通过 `DefaultStpInterfaceImpl` 提供 `StpInterface`（Sa-Token 框架契约）的空实现，返回空权限/角色列表；业务方实现自定义 `StpInterface` Bean 即可覆盖，接入 `@SaCheckPermission`/`@SaCheckRole` 注解鉴权。
- 事实粒度：单一接口契约（权限/角色数据接入）
- 适用范围：使用注解鉴权的独立认证场景
- 不适用范围：路由拦截级登录校验（不依赖 `StpInterface`）
- 证据依据：`DefaultStpInterfaceImpl.java`（`getPermissionList`/`getRoleList` 返回 `Collections.emptyList()`）、`FonsSaTokenAutoConfiguration`（`@ConditionalOnMissingBean(StpInterface.class)` 注册默认实现）、sa-token 框架 `StpInterface` 契约

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 默认空实现 | 独立认证 | sa-token | `DefaultStpInterfaceImpl` 返回空列表，注解鉴权永远不通过 | 已验证 |
| 业务方覆盖 | 独立认证 | sa-token | 业务方实现 `StpInterface` Bean 覆盖默认实现 | 已验证 |
| 权限校验 | 独立认证 | sa-token | `@SaCheckPermission("xxx")` 调用 `getPermissionList` 检查是否包含 | 已验证（sa-token 框架行为） |
| 角色校验 | 独立认证 | sa-token | `@SaCheckRole("xxx")` 调用 `getRoleList` 检查是否包含 | 已验证（sa-token 框架行为） |

## 3. 技术落地

- 入口：`@SaCheckPermission`/`@SaCheckRole` 注解（sa-token AOP 拦截）
- 应用服务：业务方实现 `StpInterface`
- 领域对象/方法：`StpInterface.getPermissionList(loginId, loginType)`、`getRoleList(loginId, loginType)`
- 仓储/Mapper：业务方自管权限/角色数据源
- 外部协作：无
- 测试：`DefaultStpInterfaceImplTest`（2 个测试验证空权限/角色列表）
