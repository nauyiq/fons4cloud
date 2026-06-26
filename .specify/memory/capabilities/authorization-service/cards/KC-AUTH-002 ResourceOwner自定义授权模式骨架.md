# KC-AUTH-002 ResourceOwner 自定义授权模式骨架

> 知识编号：KC-AUTH-002
> 知识类型：接口契约
> 所属能力：授权服务（authorization-service）
> 状态：已验证
> 来源：code
> 可信度说明：来自 OAuth2 Resource Owner base 抽象和 Password/Email/SMS 实现。
> 关联能力：自定义授权模式
> 关联适配：Password、Email、SMS
> 关联场景：CS-AUTH-005
> 关联对象：`Oauth2ResourceOwnerBaseAuthenticationConverter`、`Oauth2ResourceOwnerBaseAuthenticationProvider`、`Oauth2ResourceOwnerBaseAuthenticationToken`
> 关联代码/接口/SQL：`fons4cloud-auth/fons4cloud-auth-service/src/main/java/com/fons/cloud/auth/oauth/base/`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：Password、Email、SMS 三种自定义授权模式共享 Base Converter、Base Provider 和 Base Token 抽象，差异集中在 grant_type、请求参数校验和凭证构造。
- 事实粒度：单一接口契约。
- 适用范围：新增 Resource Owner 类授权模式时的项目内扩展骨架。
- 不适用范围：不自动定义每种授权模式的安全策略和验证码发送策略。
- 证据依据：`oauth/base/` 与 `oauth/core/password|email|sms/`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| Converter | 自定义授权模式 | Password/Email/SMS | 匹配 grant_type、校验参数、构造 Authentication Token。 | 已验证 |
| Provider | 自定义授权模式 | Password/Email/SMS | 校验客户端授权模式、scope、用户认证、Token 生成。 | 已验证 |
| Token | 自定义授权模式 | Password/Email/SMS | 保存授权模式、客户端认证、scope 和附加参数。 | 已验证 |

## 3. 技术落地

- 入口：OAuth2 Token endpoint 的 `DelegatingAuthenticationConverter`
- 应用服务：Spring Authorization Server Provider 链
- 领域对象/方法：不适用
- 仓储/Mapper：不适用
- 外部协作：AuthenticationManager、OAuth2AuthorizationService、OAuth2TokenGenerator
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 适配矩阵：`../能力适配矩阵.md`
- Password 适配：`../adaptations/password-密码授权模式能力适配说明.md`
- Email/SMS 适配：`../adaptations/email-sms-验证码授权模式能力适配说明.md`
