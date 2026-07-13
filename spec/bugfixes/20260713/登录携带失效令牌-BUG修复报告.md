# 登录携带失效令牌BUG修复报告

> Bug: `登录携带失效令牌`
> Status: Verified
> Completed: 2026-07-13

## 基本信息

- 模块/功能：`fons4cloud-admin-ui` 登录与统一 API 客户端。
- 严重级别：阻断。
- 严重级别依据：前端内存中存在失效 Access Token 时，重新登录请求会被网关提前拒绝，用户无法恢复会话。
- 影响范围：`/admin/auth/login`、`/admin/auth/refresh-token` 的匿名认证请求及重新登录流程；不改变后端 Token 生成、内省、注销和普通治理 API 鉴权。
- 报告人：Codex。

## 问题描述

- 期望结果：登录和刷新接口作为匿名认证入口，不应携带已有 Bearer Token；受保护接口和注销接口仍应携带有效 Access Token。
- 实际结果：统一 Axios 请求拦截器给包括登录接口在内的所有请求附加内存 Access Token。Token 失效后，网关在进入登录接口前返回 HTTP 401、`400001`、`invalid_bearer_token`。
- 首次发现时间：2026-07-13。
- 触发频率：必现。

## 复现步骤

1. 在前端页面内存中保存一个已经失效的 Access Token。
2. 在不刷新页面的情况下重新提交 `/admin/auth/login`。
3. 观察请求包含 `Authorization: Bearer <已失效令牌>`，网关返回 HTTP 401 和 `invalid_bearer_token`。

## 复现环境

- 环境/版本：2026-07-13 当前工作区；gateway 9527、auth-service 8001、admin-service 8002。
- 账号/角色/权限：不依赖有效账号；请求在密码认证前即被网关拒绝。
- 配置/依赖/外部条件：网关启用 OAuth2 Resource Server Bearer Token 内省。
- 日志/截图/报错信息：用户截图显示 `code=400001`、`message=令牌不存在或已过期`、`data=invalid_bearer_token`；使用无效 Bearer Token 调用登录接口得到完全一致响应。

## 根因分析

- 关键线索：不带 Authorization 的空登录请求能够到达 admin-service 参数校验；带无效 Authorization 的同一请求被网关返回完全一致的 401。
- 排查路径：截图错误码 → 网关令牌内省入口 → 登录接口公开配置 → 前端统一请求拦截器 → 无效 Token 对登录请求的等价复现。
- 根因说明：`src/api/client.ts` 的请求拦截器只判断内存中是否存在 Token，没有区分匿名认证接口。Spring Security 即使对登录路径配置 `permitAll`，仍会处理请求中主动提交的 Bearer Token，并在令牌无效时提前失败。
- 是否属于需求变更：否；修复使实现符合既有“登录/刷新接口按匿名例外控制”契约。

## 修复方案

- 修复策略：对登录和刷新路径建立精确匿名集合，发送前删除 Authorization；重新登录前主动清空旧内存 Token。
- 最小改动说明：只修改前端统一请求客户端和会话 Store；注销与普通受保护 API 继续携带 Bearer Token。
- 影响评估：不改变 HTTP 路径、请求体、响应体、Cookie、Token 格式和后端安全配置。
- 风险点：匿名路径必须使用精确匹配，不能把 `/admin/auth/logout` 一并排除，否则会影响服务端吊销当前 Access Token。
- 回滚方案：回退匿名路径判断、登录前清理及对应测试。

## 变更文件

- `fons4cloud-admin/fons4cloud-admin-ui/src/api/client.ts`：匿名认证请求删除 Authorization。
- `fons4cloud-admin/fons4cloud-admin-ui/src/stores/session.ts`：重新登录前清理旧内存 Token。
- `fons4cloud-admin/fons4cloud-admin-ui/tests/client.spec.ts`：新增鉴权头聚焦回归测试。
- `fons4cloud-admin/fons4cloud-admin-ui/tests/e2e/login.spec.ts`：增加旧 Token 场景的浏览器登录断言。
- `fons4cloud-admin/fons4cloud-admin-ui/tests/e2e/admin-api-mock.ts`：记录请求 Authorization 供 E2E 断言。
- `spec/bugfixes/20260713/登录携带失效令牌-BUG修复报告.md`：记录本次闭环证据。

## 自动化测试

- RED 证据：新增聚焦测试后，登录和刷新两条用例均收到 `Bearer stale-access-token`，结果为 2 failed、2 passed。
- 新增/更新测试：匿名登录、匿名刷新、受保护会话上下文、携带 Token 注销，以及外部 Chrome 旧 Token 重新登录。
- 测试命令：`npx vitest run tests/client.spec.ts --configLoader runner`、`npx vitest run --configLoader runner`、`npm run typecheck`、`npm run build`。
- 测试结果：聚焦测试 4/4；前端单测 9/9；类型检查通过；生产构建成功，3262 个模块完成转换。
- 若无法自动化测试，原因：不适用。

## 手动验证

1. 打开 `/admin-ui/login`，在页面内存中构造失效 Access Token。
2. 提交登录请求，在 Chrome Network 中检查 `/admin/auth/login`。
3. 确认请求没有 Authorization，登录成功后 `/admin/api/session/context` 携带新 Token。
4. 执行退出，确认 `/admin/auth/logout` 仍携带当前 Token。

预期结果：旧 Token 不再阻断重新登录；刷新接口不携带旧 Token；受保护接口和注销行为不回归。

## 回归验证

- 回归范围：统一 API 客户端、登录、刷新恢复、会话上下文、注销、浏览器 Token 持久化约束。
- 验证命令或步骤：外部系统 Google Chrome 执行 `tests/e2e/login.spec.ts`；生产构建及全量单测。
- 验证结果：外部 Chrome 2/2 通过；浏览器存储中无 Token；匿名请求无 Authorization；构建和单测全部通过。

## 证据清单

| 结论 | 证据来源 | 证据等级 | 状态 |
| --- | --- | --- | --- |
| 复现信号 | 网关等价 HTTP 复现，响应与截图完全一致 | L2 | 已验证 |
| 根因判断 | RED 测试中登录、刷新均收到旧 Bearer Token | L2/L3 | 已验证 |
| 修复已生效 | 单测 9/9、类型检查、生产构建、外部 Chrome 2/2 | L3 | 已验证 |

## 知识库同步

- Knowledge Sync Needed: no
- 影响的真理源：无；既有技术设计已经规定登录/刷新接口按匿名例外控制。
- SQL DDL files: no
- DDL grouping: 不适用。
- Suggested follow-up: none

## 后续事项

- Client Secret 高熵字符串替换按用户决定后续独立处理，不属于本次 BUG。
- 密码明文经 HTTPS 传输与 bcrypt 校验的调整不属于本次 BUG。
