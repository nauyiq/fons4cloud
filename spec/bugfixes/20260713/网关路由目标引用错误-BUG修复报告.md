# 网关路由目标引用错误-BUG修复报告

> Bug: `网关路由目标引用错误`
> Status: Verified
> Completed: 2026-07-13

## 基本信息

- 模块/功能：`fons4cloud-admin-service` 网关路由治理发布。
- 严重级别：阻断。
- 影响范围：以单条路由 ID 创建草稿时，会将路由 ID 误作 Nacos dataId，无法对权威 `gateway-routing.json` 执行预期发布。

## 问题描述

- 期望结果：路由 ID 仅定位数组元素，所有网关路由读写都使用配置的权威 dataId。
- 实际结果：`GovernancePublishService` 为资源构造 `targetRef=resourceKey`，而适配器优先使用 targetRef，导致真实路由 ID 被错误作为 dataId。

## 复现步骤

1. 以非 `gateway-routing.json` 的路由 ID 构造 `ResourceRef`。
2. 调用 `GatewayRouteGovernanceAdapter.loadCurrent`。
3. 修复前会尝试读取该路由 ID 对应的 Nacos dataId，而非配置的权威路由数组。

## 复现环境

- 环境/版本：2026-07-13，外部启动 gateway、auth-service、admin-service；admin-service 使用 JDK 21 新构建 JAR。
- 账号/角色/权限：已绑定且启用的管理员账号，具备网关与变更操作权限。
- 配置/依赖/外部条件：Nacos 中 `gateway-routing.json` 为网关动态路由权威配置。

## 根因分析

- 网关动态路由实际使用一个 Nacos dataId 保存完整路由数组。
- 通用治理资源的 targetRef 语义与网关“数组内路由 ID”语义被混用。
- 这属于实现缺陷，不改变既有需求或接口契约。

## 修复方案

- `GatewayRouteGovernanceAdapter` 对网关域始终使用构造时配置的 dataId；保留 resourceKey 仅作为路由标识。
- 新增聚焦测试，覆盖 resourceKey、targetRef 都为普通路由 ID 时仍读取 `gateway-routing.json`。
- 回滚方案：回退适配器 dataId 解析逻辑及新增测试；真实验证中的临时路由已通过发布前快照回滚并确认清理。

## 变更文件

- `fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/infrastructure/nacos/GatewayRouteGovernanceAdapter.java`
- `fons4cloud-admin/fons4cloud-admin-service/src/test/java/com/fons/cloud/admin/infrastructure/nacos/GatewayRouteGovernanceAdapterTest.java`

## 自动化测试

- RED 证据：新增普通路由 ID 的 dataId 断言，修复前会读取错误 dataId。
- 验证命令：`mvn -pl fons4cloud-admin/fons4cloud-admin-service -am -Dtest=GatewayRouteGovernanceAdapterTest,GovernancePublishServiceTest,AdminSecurityInterceptorTest,AdminResourceAnnotationBoundaryTest -Dsurefire.failIfNoSpecifiedTests=false test`
- 结果：JDK 21 Reactor 23 个模块成功，聚焦测试 21/21 通过。

## 手动验证

1. 用新 JAR 重启 8002。
2. 创建隔离路径临时路由草稿并校验成功。
3. 发布后回读路由可见，发布前快照存在。
4. 用过期基线再次发布，结果为 `DRIFT_DETECTED`，未覆盖权威配置。
5. 以发布前快照回滚；首次同步回读为 `PENDING_CONFIRM` 后，调用只回读恢复接口确认成功。

预期结果：临时路由清理后，路由数恢复为 8，权威摘要恢复为发布前值。实际结果符合预期。

## 回归验证

- 真实 HTTP：登录、草稿、校验、发布、回读、漂移检测、回滚、恢复确认和清理均完成。
- 外部 Chrome：系统 Chrome 的真实登录、加载会话和退出 Playwright 用例 1/1 通过。
- Maven package：23 个 Reactor 模块成功。

## 证据清单

| 结论 | 证据来源 | 证据等级 | 状态 |
| --- | --- | --- | --- |
| 复现信号 | 普通路由 ID 会进入 targetDataId 的 targetRef 分支 | L2 | 已验证 |
| 根因判断 | 适配器 targetDataId 分支与普通路由 ID 测试 | L2 | 已验证 |
| 修复已生效 | 聚焦测试 21/21、Maven package、真实临时路由发布与回滚恢复 | L3 | 已验证 |

## 知识库同步

- Knowledge Sync Needed: yes
- SQL DDL files: no
- Suggested follow-up: 用户显式触发 `fons4ai-knowledge-summary` 后同步网关路由“单 dataId 完整数组”事实。

## 后续事项

- Nacos 管理 API 在当前地址返回兼容层错误，尚未完成“外部直接改写后”的漂移验证；本次已验证过期基线漂移，不能替代该项证据。
