# KC-AUTH-004 权限资源注册与校验

> 知识编号：KC-AUTH-004
> 知识类型：治理规则
> 所属能力：授权服务（authorization-service）
> 状态：已验证
> 来源：code
> 可信度说明：来自 auth-core 权限资源仓库和权限校验抽象源码。
> 关联能力：权限资源校验
> 关联适配：Redis 权限资源
> 关联场景：CS-AUTH-007
> 关联对象：`AuthPermissionService`、`AbstractAuthPermissionService`、`AuthorizationResourceRepository`
> 关联代码/接口/SQL：`fons4cloud-auth/fons4cloud-auth-core/src/main/java/com/fons/cloud/auth/`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：权限资源校验以 `AuthenticationRequest` 为输入，先放行 OPTIONS、静态白名单、白名单 IP、业务白名单 URI，再按 Redis 中的资源 ID 与 authorities 判断请求是否允许访问。
- 事实粒度：单一治理规则。
- 适用范围：资源服务、网关或业务服务的请求权限校验。
- 不适用范围：不定义业务权限模型、角色命名规范和资源扫描治理。
- 证据依据：`AbstractAuthPermissionService.java`、`AuthorizationResourceRepository.java`、`StaticEndpointAuthorizationManager.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 白名单 | 权限校验 | Redis 权限资源 | OPTIONS、静态端点、白名单 IP、业务白名单 URI 优先放行。 | 已验证 |
| 资源 ID | 权限校验 | Redis 权限资源 | HTTP 资源默认使用 method + `_` + uri。 | 已验证 |
| 权限匹配 | 权限校验 | Redis 权限资源 | 资源未配置规则时放行；配置后要求用户 authorities 与资源 authorities 有交集。 | 已验证 |
| 旧注解 | 权限校验 | AOP 旧路径 | `PreAuthentication` 和 `AuthenticationAspect` 已标记 Deprecated。 | 已验证 |

## 3. 技术落地

- 入口：`AuthPermissionService.isPermitRequest`
- 应用服务：资源服务、网关或业务服务
- 领域对象/方法：`AuthenticationRequest`
- 仓储/Mapper：`AuthorizationResourceRepository`
- 外部协作：Redis、ManualWhiteIpService
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../授权服务能力文档.md`
- 运行文档：`../授权服务运行文档.md`
