# 登录后跳回登录页BUG修复报告

> Bug: `登录后跳回登录页`
> Status: Verified
> Completed: 2026-07-10

## 基本信息

- 模块/功能：`fons4cloud-admin-service` 内置控制台登录态管理
- 严重级别：高
- 严重级别依据：管理员密码登录成功后无法稳定使用控制台，页面导航会中断核心治理操作。
- 影响范围：`/admin-ui/**` 页面使用密码模式登录后的页面导航和前端 Token 刷新判断；不影响认证服务 Token 生成、后端 API 契约和数据库结构。
- 报告人：Codex

## 问题描述

- 期望结果：管理员登录成功后，在 access token 有效期内可以持续访问控制台页面；Token 到期时才调用刷新接口。
- 实际结果：登录成功后点击控制台导航，页面重新加载脚本并把 UUID reference token 当作无效 JWT，错误触发刷新；刷新失败后清理会话并跳回登录页。
- 首次发现时间：2026-07-10
- 触发频率：必现

## 复现步骤

1. 使用管理员账号通过 `/admin-ui/login` 执行密码登录。
2. 登录成功后保存 auth-service 返回的 UUID reference access token，并进入 `/admin-ui`。
3. 点击任意控制台导航，页面脚本重新执行登录态检查；修复前会调用 `/admin/auth/refresh-token`，刷新失败时跳转 `/admin-ui/login` 并清除 access token。

## 复现环境

- 环境/版本：2026-07-10 当前工作区；JDK 21；auth-service 使用 `OAuth2TokenFormat.REFERENCE`。
- 账号/角色/权限：用户提供的管理员账号；凭据未写入源码、测试或报告。
- 配置/依赖/外部条件：`admin-service` 内置 Freemarker 页面，Token 存储于 `sessionStorage`。
- 日志/截图/报错信息：等价 JavaScript 运行实验中，UUID access token 导致请求 `/admin/auth/refresh-token`、跳转 `/admin-ui/login`，并从会话移除 access token。

## 根因分析

- 关键线索：`DefaultOauth2AccessTokenGenerator` 为 reference token 生成 UUID；`admin-console.js` 的 `isTokenExpired` 却要求 Token 必须为三段 JWT，并读取 JWT `exp`。
- 排查路径：用户现象 → 页面导航重新加载 → `requireLogin` → `isTokenExpired` → reference token/JWT 格式对比 → 等价运行复现。
- 根因说明：前端错误假设 access token 一定是 JWT，违反了认证服务已存在的 reference token 契约。任何 UUID token 都被判为过期，进而错误刷新和退出登录。
- 是否属于需求变更：否

## 修复方案

- 修复策略：使用登录/刷新响应已经提供的 `expiresIn` 计算并保存 access token 过期时间，不再解析 access token 内容。
- 最小改动说明：新增一个 `sessionStorage` 过期时间键；保存和清理 Token 时同步维护；`requireLogin` 仅根据该过期时间决定是否刷新。旧会话没有过期时间时交由后端 401/403 作为最终安全边界。
- 影响评估：不改变登录、刷新、吊销 API，不改变 Token 格式和后端认证授权逻辑。
- 风险点：客户端时间不准确可能使主动刷新稍早或稍晚；后端仍会通过 401/403 拒绝失效 Token 并触发登录跳转。
- 回滚方案：回退 `admin-console.js` 的过期时间存储和判断逻辑，以及对应回归测试。

## 变更文件

- `fons4cloud-admin/fons4cloud-admin-service/src/main/resources/static/admin/js/admin-console.js`：改用 `expiresIn` 管理 reference token 有效期。
- `fons4cloud-admin/fons4cloud-admin-service/src/test/java/com/fons/cloud/admin/interfaces/page/AdminPageControllerTest.java`：新增 Token 生命周期脚本契约回归测试。
- `spec/bugfixes/20260710/登录后跳回登录页-BUG修复报告.md`：记录复现、根因、修复和验证证据。

## 自动化测试

- RED 证据：新增测试后，`adminConsoleShouldUseServerExpiresInForReferenceTokenLifetime` 因脚本不包含 `accessTokenExpiresAt`/`response.expiresIn` 且仍解析 JWT 而按预期失败；测试结果为 4 个测试、1 个失败。
- 新增/更新测试：`AdminPageControllerTest#adminConsoleShouldUseServerExpiresInForReferenceTokenLifetime`。
- 测试命令：`mvn -pl fons4cloud-admin/fons4cloud-admin-service -am -Dtest=AdminPageControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`（JDK 21）。
- 测试结果：修复后聚焦测试 4 个全部通过；admin-service 及依赖模块完整回归 91 个测试全部通过。
- 若无法自动化测试，原因：不适用。

## 手动验证

1. 启动 gateway、auth-service、admin-service 及其 Nacos、Redis、数据库依赖。
2. 使用已提供的管理员账号访问 `/admin-ui/login` 并完成密码登录。
3. 依次点击“概览、服务治理、网关治理、流量治理、授权资源、认证客户端、变更中心、审计追踪、运行探测”。
4. 在浏览器会话存储中确认 access token 为 UUID/reference token 时页面不会立即请求刷新接口或跳回登录页。
5. 将 `fons4cloud.admin.accessTokenExpiresAt` 调整为过去时间并刷新业务页，确认页面调用刷新接口；刷新成功后更新 Token 和过期时间。

预期结果：有效 reference token 可以跨页面导航持续使用；仅到期后主动刷新；后端返回 401/403 时清理会话并跳回登录页。

## 回归验证

- 回归范围：控制台页面路由资源、登录态存储、Bearer Token 注入、Token 刷新入口以及 admin-service 全部单元测试。
- 验证命令或步骤：JDK 21 下执行 admin-service 聚焦测试和 `mvn -pl fons4cloud-admin/fons4cloud-admin-service -am test`；执行 UUID reference token 等价 JavaScript 行为实验。
- 验证结果：聚焦测试 4/4 通过；完整回归 91/91 通过；行为实验中刷新请求 0 次、登录页跳转 0 次、UUID access token 保留。

## 证据清单

| 结论 | 证据来源 | 证据等级 | 状态 |
| --- | --- | --- | --- |
| 复现信号 | 修复前 UUID token 等价 JavaScript 运行实验：刷新请求 1 次、跳转登录页 1 次、Token 被清除 | L2 | 已验证 |
| 根因判断 | `DefaultOauth2AccessTokenGenerator` UUID reference token 与 `admin-console.js` JWT 解码逻辑对比 | L2 | 已验证 |
| 修复已生效 | 聚焦测试 4/4、完整回归 91/91、修复后行为实验无刷新/无跳转 | L3 | 已验证 |

## 知识库同步

- Knowledge Sync Needed: no
- 影响的真理源：无；本次修复使页面实现重新符合既有 reference token 和 `expiresIn` 契约。
- SQL DDL files: no
- DDL grouping: 不适用
- Suggested follow-up: none

## 后续事项

- 在依赖服务可用的真实联调环境按“手动验证”步骤再执行一次管理员页面全链路冒烟；本次代码级和等价行为验证已关闭该缺陷。
