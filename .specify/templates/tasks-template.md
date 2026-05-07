---
description: "Task list template for S1/S2 feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`  
**Prerequisites**: `spec.md`、`plan.md`，以及 S2 按需使用的 `research.md`、`data-model.md`、`contracts/`、`quickstart.md`  
**SDD Level**: `[S1/S2]`

**Note**: 生成内容必须使用中文。S0 轻量变更不强制生成 `tasks.md`；行为变更默认必须包含测试或等价验证任务。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行执行，必须满足不同文件且无依赖冲突。
- **[Story]**: 对应用户故事，例如 US1、US2、US3。
- 每个任务必须包含精确文件路径。
- 如果任务会编辑同一文件，不得标记为 `[P]`。
- 行为变更必须按 Red-Green-Refactor 表达。

## 实施前门禁

- [ ] T001 [Gate] 对照 `.specify/memory/constitution.md` 确认 SDD 等级、事实调研、复用优先和用户确认要求
- [ ] T002 [Gate] 对照 `.specify/memory/java-development-standard.md` 确认 Java 规范适用项
- [ ] T003 [Gate] 阅读相关 Java 文件、Mapper XML、POM、配置、调用方和测试
- [ ] T004 [Gate] 确认若修改或删除已有行为，已获得用户明确确认
- [ ] T005 [Gate] 确认当前任务仍符合 S1/S2；若影响升级，先回到 spec/plan 更新等级

## Phase 1: Setup

**Purpose**: 准备本次功能所需的最小结构、测试环境和依赖确认。

- [ ] T006 [Setup] 确认本次变更涉及的模块和路径
- [ ] T007 [Setup] 确认受影响模块的构建和测试命令

---

## Phase 2: Foundational

**Purpose**: 完成所有用户故事共享的基础工作。此阶段完成前不得进入用户故事实现。

- [ ] T008 [Foundation] 确认错误码、响应模型、日志、安全、缓存、事务策略
- [ ] T009 [Foundation] 确认是否需要新增或复用 Converter、Entity、Mapper、Service、Facade
- [ ] T010 [Foundation] 确认 Mapper XML 是否需要显式列名和 SQL 条件括号
- [ ] T011 [Foundation] S2 若涉及公共抽象、迁移、回滚或外部契约，补齐对应设计和验证任务

**Checkpoint**: 基础设计完成，可以进入用户故事任务。

---

## Phase 3: User Story 1 - [Title] (Priority: P1)

**Goal**: [该故事交付的价值]  
**Independent Test**: [独立验证方式]

### Red: 用户故事 1 的失败测试或等价验证

- [ ] T012 [P] [US1] 在 [test path] 新增失败测试或记录等价验证步骤
- [ ] T013 [P] [US1] 覆盖边界、异常或安全场景

### Green: 用户故事 1 的最小实现

- [ ] T014 [US1] 在 [source path] 实现最小业务逻辑
- [ ] T015 [US1] 在 [source path] 接入统一响应、异常码、日志和必要校验
- [ ] T016 [US1] 在 [mapper/config path] 完成必要持久化、缓存或配置变更

### Refactor & Verify: 用户故事 1

- [ ] T017 [US1] 对照 Java 开发规范检查命名、分层、异常、日志、数据库、缓存和安全
- [ ] T018 [US1] 运行 plan.md 中记录的验证命令或执行人工验证步骤

**Checkpoint**: 用户故事 1 可独立交付和验证。

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [该故事交付的价值]  
**Independent Test**: [独立验证方式]

### Red: 用户故事 2 的失败测试或等价验证

- [ ] T020 [P] [US2] 在 [test path] 新增失败测试或记录等价验证步骤

### Green: 用户故事 2 的最小实现

- [ ] T021 [US2] 在 [source path] 实现最小业务逻辑
- [ ] T022 [US2] 在 [source path] 完成与 US1 的必要集成

### Refactor & Verify: 用户故事 2

- [ ] T023 [US2] 对照 Java 开发规范完成合规检查
- [ ] T024 [US2] 运行 plan.md 中记录的验证命令或执行人工验证步骤

**Checkpoint**: 用户故事 2 可独立交付和验证。

---

## Phase N: Final Verification

- [ ] TXXX [Verify] 检查接口是否统一返回 `R<T>` 或项目既有响应模型
- [ ] TXXX [Verify] 检查业务异常是否使用 `BizException` + `ResultCode` / `XxxResultCode`
- [ ] TXXX [Verify] 检查 Entity 是否未直接暴露给外部接口
- [ ] TXXX [Verify] 检查 Mapper XML 是否避免 `SELECT *`，`AND` / `OR` 混用是否加括号
- [ ] TXXX [Verify] 检查日志是否参数化且未输出敏感信息
- [ ] TXXX [Verify] 补充或更新必要文档
- [ ] TXXX [Verify] 运行最终验证命令并记录结果、残余风险和未验证原因

## Dependencies & Execution Order

1. 实施前门禁和 Setup 必须先完成。
2. Foundational 阶段阻塞所有用户故事。
3. 每个用户故事内部必须先 Red，再 Green，再 Refactor & Verify。
4. 不同用户故事可在基础阶段完成后并行，但不得编辑同一文件或破坏独立验收。
5. Final Verification 必须在目标用户故事完成后执行。

## Notes

- `[P]` 只用于不同文件且无依赖冲突的任务。
- 每个任务必须能被执行者独立理解。
- 避免模糊任务，例如“优化代码”“完善逻辑”。
- 若无法自动化测试，必须记录人工验证步骤和风险原因。
- S1 的 checklist 内容已合并到门禁和最终验证中；S2 若需要独立检查清单，使用 `checklist-template.md`。
