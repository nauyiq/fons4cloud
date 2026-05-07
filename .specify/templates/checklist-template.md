# [CHECKLIST TYPE] Checklist: [FEATURE NAME]

**Purpose**: [说明该检查清单覆盖的范围]  
**Created**: [DATE]  
**Feature**: [Link to spec.md or relevant documentation]  
**SDD Level**: `S2`

**Note**: 此检查清单仅在 S2 复杂能力中默认使用。S0 不生成独立 checklist；S1 将检查项合并到 `plan.md` 和 `tasks.md`。生成内容必须使用中文。

<!--
生成 checklist 时必须替换下方样例项，并结合当前 feature 的真实上下文。
-->

## 分级与调研证据

- [ ] CHK001 已确认本功能属于 S2 复杂能力，且升级原因记录在 spec.md 和 plan.md
- [ ] CHK002 已列出实施前阅读过的相关代码、配置、接口、Mapper XML、POM 和测试文件
- [ ] CHK003 关键判断均可追溯到仓库事实或用户确认
- [ ] CHK004 与用户描述不一致的现状已明确记录

## 简洁性

- [ ] CHK005 方案是满足当前需求的最简单可维护实现
- [ ] CHK006 新增抽象、依赖、模块或配置均有必要性说明
- [ ] CHK007 被拒绝的更简单替代方案已说明原因

## 模块复用

- [ ] CHK008 已优先复用现有工具函数、组件、starter 和公共模块
- [ ] CHK009 变更放置在职责匹配的模块边界内
- [ ] CHK010 未重复实现已有认证、网关、消息、缓存、限流、锁或数据访问能力

## Java 开发规范

- [ ] CHK011 已阅读 `.specify/memory/java-development-standard.md`
- [ ] CHK012 Spring Bean 使用构造注入，优先 `@RequiredArgsConstructor` + `final`
- [ ] CHK013 对外接口统一返回 `R<T>` 或项目既有响应模型
- [ ] CHK014 业务异常使用 `BizException` + `ResultCode` / `XxxResultCode`
- [ ] CHK015 Entity 未直接暴露给外部接口，已通过 Converter 转换
- [ ] CHK016 Mapper XML 避免 `SELECT *`，`AND` / `OR` 混用已加括号
- [ ] CHK017 日志使用参数化写法，未输出敏感信息
- [ ] CHK018 密码、token、身份证、手机号全量、clientSecret 等敏感字段未明文泄露

## 复杂能力风险

- [ ] CHK019 公共 API、配置契约或跨模块调用的兼容性影响已说明
- [ ] CHK020 数据库、缓存、MQ、事务或迁移影响已说明
- [ ] CHK021 安全、认证、网关、限流或权限影响已说明
- [ ] CHK022 回滚策略、迁移策略或人工补救步骤已记录

## 测试先行

- [ ] CHK023 行为变更已有先失败的测试或等价验证步骤
- [ ] CHK024 每个用户故事可独立验收
- [ ] CHK025 验证命令、人工验证步骤和残余风险已记录

## 确认门禁

- [ ] CHK026 删除或修改既有行为已获得用户确认
- [ ] CHK027 复杂度、兼容性或迁移风险已在计划中说明
- [ ] CHK028 未触碰无关未提交改动

## 中文文档

- [ ] CHK029 规格、计划、任务和检查清单使用中文描述
- [ ] CHK030 默认假设、范围外事项和后续事项表述清楚

## Notes

- 已完成项使用 `[x]` 标记。
- 可在检查项后补充证据路径、验证命令或风险说明。
- 检查项必须按当前 feature 实际情况增删，但不得删除宪章和 Java 规范相关门禁。
