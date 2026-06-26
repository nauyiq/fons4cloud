# KC-AUTH-001 OAuth2 授权服务公共骨架

> 知识编号：KC-AUTH-001
> 知识类型：技术流程
> 所属能力：授权服务（authorization-service）
> 状态：已验证
> 来源：code
> 可信度说明：来自授权服务器配置、认证应用服务和 Token 生成源码。
> 关联能力：OAuth2 授权服务
> 关联适配：Password/Email/SMS、Redis Token 存储
> 关联场景：CS-AUTH-001、CS-AUTH-004、CS-AUTH-005
> 关联对象：`AuthorizationServerAutoConfiguration`、`AuthenticationApplicationService`
> 关联代码/接口/SQL：`fons4cloud-auth/fons4cloud-auth-service/src/main/java/com/fons/cloud/auth/`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：授权服务同时提供 HTTP OAuth2 授权服务器链路和脱离 HTTP 上下文的 RPC 认证应用服务链路，两者都围绕 OAuth Client、账号用户、scope、Token 生成和 `OAuth2AuthorizationService` 存储展开。
- 事实粒度：单一技术流程。
- 适用范围：认证、刷新 Token、吊销 Token、Token endpoint。
- 不适用范围：不定义生产安全策略、客户端审批流程和网关流量治理。
- 证据依据：`AuthorizationServerAutoConfiguration.java`、`AuthenticationApplicationService.java`、`AuthenticationApplicationServiceImpl.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 客户端校验 | OAuth2 授权服务 | 全部认证模式 | clientId/clientSecret 和授权模式必须合法。 | 已验证 |
| Scope 校验 | OAuth2 授权服务 | 全部认证模式 | 请求 scope 必须在 OAuth Client 允许范围内。 | 已验证 |
| Token 生成 | OAuth2 授权服务 | 全部认证模式 | 使用项目自定义 reference access token 生成器。 | 已验证 |
| Token 存储 | OAuth2 授权服务 | Redis Token 存储 | 生成后保存 `OAuth2Authorization`。 | 已验证 |

## 3. 技术落地

- 入口：OAuth2 Token endpoint、`AccountAuthenticationFacadeService.authenticate`
- 应用服务：`AuthenticationApplicationServiceImpl`
- 领域对象/方法：`Account`、`OauthClient`
- 仓储/Mapper：`AccountDomainService`、`SysOauthClientDomainService`
- 外部协作：Redis、Dubbo、Spring Authorization Server
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../授权服务能力文档.md`
- 运行文档：`../授权服务运行文档.md`
- 相关卡片：`KC-AUTH-002 ResourceOwner自定义授权模式骨架.md`
