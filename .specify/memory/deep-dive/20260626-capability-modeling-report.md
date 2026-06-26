# 技术能力知识建模报告

> 更新日期：2026-06-26
> 执行范围：单能力域

## 1. 本次建模范围

| 能力域 | 建模状态 | 风险等级 | 主要证据 | 输出文档 |
| --- | --- | --- | --- | --- |
| 授权服务（authorization-service） | 已完成项目级深挖 | P0 | auth-service/auth-core/auth-spring-security/auth-service-api 源码、POM、应用配置、OAuth2 授权服务器配置、Redis Token 存储、RPC Facade | `.specify/memory/capabilities/authorization-service/` |

## 2. 处理顺序

| 顺序 | 能力域 | 排序原因 |
| --- | --- | --- |
| 1 | 授权服务 | 用户明确选择只建模授权服务；项目基线将认证鉴权、OAuth2/Token 支撑列为 P0。 |

## 3. 未处理能力域

| 能力域 | 原因 | 建议动作 |
| --- | --- | --- |
| 网关鉴权与网关安全 | 用户本轮选择只建模授权服务。 | 后续可单独建模 `gateway-security` 或 `gateway-routing-traffic-governance`。 |
| 网关接入与动态路由 | 不属于本轮授权服务边界。 | 后续按网关能力单独建模。 |
| 限流治理与网关流控 | 不属于本轮授权服务边界。 | 后续按 P1 能力建模。 |

## 4. 需要用户确认的问题

| 编号 | 能力域 | 问题 | 影响 | 建议处理 |
| --- | --- | --- | --- | --- |
| Q-AUTH-001 | 授权服务 | OAuth Client、scope、authorities 是否存在团队级命名和授权规范。 | 接入治理和权限一致性。 | 后续结合安全规范确认。 |
| Q-AUTH-002 | 授权服务 | Email/SMS 验证码发送、频控、重试和过期策略未确认。 | 验证码授权模式安全性。 | 后续建模验证码或缓存能力。 |
| Q-AUTH-003 | 授权服务 | Redis Token 序列化开关和单用户 Token 数量限制开关生产值未确认。 | Token 兼容性和多端登录策略。 | 后续结合配置中心事实确认。 |
