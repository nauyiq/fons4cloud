# KC-AUTH-005 账号认证 RPC Facade

> 知识编号：KC-AUTH-005
> 知识类型：接口契约
> 所属能力：授权服务（authorization-service）
> 状态：已验证
> 来源：code
> 可信度说明：来自 auth-service-api 接口和 auth-service Dubbo 实现源码。
> 关联能力：账号认证 RPC Facade
> 关联适配：Dubbo RPC 认证服务暴露
> 关联场景：CS-AUTH-001、CS-AUTH-002、CS-AUTH-003
> 关联对象：`AccountAuthenticationFacadeService`、`AccountAuthenticationFacadeServiceImpl`
> 关联代码/接口/SQL：`fons4cloud-auth/fons4cloud-auth-service-api/src/main/java/com/fons/cloud/account/service/AccountAuthenticationFacadeService.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：账号认证 RPC Facade 对外提供认证、刷新 Token、吊销 Token 三个接口，当前通过 Dubbo `@DubboService` 暴露，并委托 `AuthenticationApplicationService` 执行。
- 事实粒度：单一接口契约。
- 适用范围：内部服务需要通过 RPC 获取、刷新或吊销 Token。
- 不适用范围：不定义服务调用权限、熔断重试和网关 HTTP 接口。
- 证据依据：`AccountAuthenticationFacadeService.java`、`AccountAuthenticationFacadeServiceImpl.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 认证 | RPC Facade | Dubbo | `authenticate` 返回 `R<TokenInfo>`。 | 已验证 |
| 刷新 | RPC Facade | Dubbo | `refreshToken` 返回新 `R<TokenInfo>`。 | 已验证 |
| 吊销 | RPC Facade | Dubbo | `revokeToken` 返回 `R<Boolean>`。 | 已验证 |
| 编排 | RPC Facade | Dubbo | Facade 不直接生成 Token，委托应用服务。 | 已验证 |

## 3. 技术落地

- 入口：`AccountAuthenticationFacadeService`
- 应用服务：`AuthenticationApplicationService`
- 领域对象/方法：`AuthenticateRequest`、`RefreshTokenRequest`、`TokenInfo`
- 仓储/Mapper：不直接访问
- 外部协作：Dubbo
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../授权服务能力文档.md`
- 适配矩阵：`../能力适配矩阵.md`
