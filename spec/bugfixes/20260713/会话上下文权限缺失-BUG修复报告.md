# 会话上下文权限缺失BUG修复报告

> Bug: `会话上下文权限缺失`
> Status: Verified
> Completed: 2026-07-13

## 基本信息

- 模块/功能：`fons4cloud-admin-service` 会话上下文与内部 RBAC。
- 严重级别：阻断。
- 影响范围：已登录管理员请求 `GET /admin/api/session/context` 时返回 403，SPA 无法加载登录后的会话信息。

## 问题描述

- 期望结果：已绑定且启用的管理员可读取自身会话上下文，由返回的权限集合决定页面可见菜单。
- 实际结果：真实登录成功并签发 Access Token 后，请求会话上下文返回 403。

## 复现步骤

1. 通过网关调用 `/admin/auth/login`，获得 Access Token 和 Refresh Cookie。
2. 使用 Access Token 调用 `/admin/api/session/context`。
3. 修复前返回 HTTP 403。

## 复现环境

- 环境/版本：2026-07-13 外部启动的 gateway 9527、auth-service 8001、admin-service 8002。
- 配置/依赖/外部条件：管理员账号已在 admin RBAC 中绑定；网关已完成 Bearer Token 内省。
- 日志/报错信息：登录请求成功，随后 `/admin/api/session/context` 返回 HTTP 403。

## 根因分析

- `AdminSessionController#context` 声明了网关 `ADMIN` 权限，但没有 `@AdminPermission`。
- `AdminSecurityInterceptor` 对未声明内部 RBAC 的 MVC 接口按默认拒绝处理，因此请求在进入 Controller 前被拒绝。
- 这不是权限数据缺失：admin-service 日志已经显示管理员绑定、角色和权限查询均可执行。

## 修复方案

- 在会话上下文接口显式声明 `@AdminPermission(authorities = {})`。
- 约定显式空数组仅表示“已绑定且启用的管理员可访问”；未声明注解的接口继续默认拒绝。
- 新增 `AdminAuthorizationService#authorizeAdmin` 复用管理员绑定与启用状态校验，不为会话接口伪造功能域权限。
- 回滚方案：回退会话接口的显式空权限声明、拦截器分支和对应测试；恢复后会话上下文将重新按默认拒绝处理。

## 变更文件

- `fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/interfaces/rest/api/AdminSessionController.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/infrastructure/security/AdminSecurityInterceptor.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/infrastructure/security/AdminAuthorizationService.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/infrastructure/security/AdminPermission.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/test/java/com/fons/cloud/admin/infrastructure/security/AdminSecurityInterceptorTest.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/test/java/com/fons/cloud/admin/infrastructure/security/AdminResourceAnnotationBoundaryTest.java`

## 自动化测试

- RED 证据：新增“显式空权限允许已绑定管理员”测试后，因 `authorizeAdmin` 不存在而编译失败。
- GREEN 命令：`mvn -pl fons4cloud-admin/fons4cloud-admin-service -am -Dtest=AdminSecurityInterceptorTest,AdminResourceAnnotationBoundaryTest -Dsurefire.failIfNoSpecifiedTests=false test`。
- GREEN 结果：Reactor 23 个模块成功；聚焦测试 7/7 通过。

## 手动验证

1. 使用新构建的 admin-service 替换当前 8002 进程。
2. 经 9527 登录并调用 `/admin/api/session/context`。
3. 预期登录、会话上下文、刷新、注销均返回成功；未标注 `@AdminPermission` 的测试接口仍返回 403。

## 回归验证

- 回归范围：内部 RBAC 默认拒绝、已绑定管理员访问、现有功能域权限接口。
- 验证命令或步骤：执行 `AdminSecurityInterceptorTest` 与 `AdminResourceAnnotationBoundaryTest`。
- 验证结果：聚焦测试 7/7 通过；待新 JAR 部署后补充真实 HTTP 结果。

## 证据清单

| 结论 | 证据来源 | 证据等级 | 状态 |
| --- | --- | --- | --- |
| 复现信号 | 真实登录成功后会话上下文 HTTP 403 | L2 | 已验证 |
| 根因判断 | Controller 注解与拦截器默认拒绝逻辑对照 | L2 | 已验证 |
| 修复已生效 | JDK 21 Reactor 聚焦测试 7/7 通过 | L3 | 已验证 |

## 知识库同步

- Knowledge Sync Needed: no
- SQL DDL files: no
- Suggested follow-up: 在部署新 JAR 后完成真实 HTTP 闭环。

## 后续事项

- 已于 2026-07-13 使用最新 JAR 重启 8002 并完成真实复验：登录、`/admin/api/session/context`、刷新和退出均返回成功；系统 Chrome 真实登录、会话加载和退出用例 1/1 通过。
- T020/T021 仍需补齐外部直接修改 Nacos 后的漂移证据及隔离环境审计证据。
