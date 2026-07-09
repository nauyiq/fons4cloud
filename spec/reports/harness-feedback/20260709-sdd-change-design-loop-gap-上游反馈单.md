# SDD Change 缺少设计 Loop 与跨模块方案输出上游反馈单

> Feedback ID: `20260709-sdd-change-design-loop-gap`
> Status: Draft
> Created: 2026-07-09
> Default path: `spec/reports/harness-feedback/20260709-sdd-change-design-loop-gap-上游反馈单.md`

## 状态判定

- 当前状态为 `Draft`。
- 原因：本反馈基于一个框架类试点项目的一次 SDD change 到实现体验，现象和证据可定位，但尚未形成多项目重复证据；跨项目重复性待观察。
- 本单用于上游评估 `fons4ai-sdd-change`、CR 模板、任务规划模板和实现交接规则是否需要增强，不等同于已批准修改上游技能库。

## 来源

- 来源项目：匿名框架类基础设施项目。
- 来源任务：既有控制面功能的增量变更，涉及 gateway 认证逻辑优化与 admin 双层权限边界调整。
- 关联报告：
  - `spec/features/20260630/changes/CR-001.md`
  - `spec/features/20260630/框架控制面-任务规划.md`
  - `spec/features/20260630/reports/框架控制面-实施报告.md`
- 关联技能：
  - `fons4ai-sdd-change`
  - `fons4ai-sdd-implement`
  - `fons4ai-harness-feedback`
- 关联模板/脚本：
  - `fons4ai-sdd-change/assets/templates/change-template.md`
  - `fons4ai-sdd-tasks/scripts/validate_sdd_artifacts.py`
  - `fons4ai-harness-feedback/assets/templates/upstream-feedback-template.md`
  - `fons4ai-harness-feedback/scripts/validate_harness_feedback.py`

## 问题分类

已选分类：

- SKILL_CONTRACT
- TEMPLATE_GAP
- EVIDENCE_GAP
- CONTEXT_LOADING
- VALIDATOR_GAP

## 现象

- 使用 SDD change 技能处理既有功能变更时，CR 很快生成并同步到了原控制面技术设计和任务规划，但没有形成面向被影响模块的独立技术方案或模块级设计说明。
- 本次变更明确影响 gateway 服务的认证授权边界，但输出主要落在控制面 feature 目录中，未强制产出 gateway 侧的方案边界、现有框架机制复用分析、替代方案比较和不可改边界。
- CR 中虽然列出了代码影响范围和 T023-T026，但任务层面对“是否应复用原有 `@AuthenticationResource(authorities = "ADMIN")` 机制”缺少设计确认门禁，导致后续实现阶段直接进入编码。
- 用户没有参与关键设计 Loop：例如 gateway 层权限究竟通过路径级硬编码、配置化 authority，还是通过既有资源注解注册机制表达；admin 内部 RBAC 与 gateway 粗粒度准入如何组合。
- 实现阶段根据不充分的 CR 任务直接修改代码，引入了较多设计偏差：gateway 出现路径级 `/admin/**` admin authority 判断；admin Controller 移除了公共资源注解，和用户原始设想“接口用 `@AuthenticationResource(authorities = "ADMIN")` 表达 gateway 准入”不一致。
- 实施报告记录了 T023-T026 的代码级完成证据，但这些证据主要证明测试通过，没有证明改造方案已经经过用户参与的边界选择和设计确认。

## 期望行为

- SDD change 在识别到权限、安全、gateway、auth、admin 等跨核心模块变更时，应先进入设计 Loop，而不是直接生成可执行增量任务。
- change 阶段应显式输出“受影响模块设计清单”，至少包含每个受影响模块的职责、复用机制、禁止改动边界、替代方案和推荐方案。
- 对已有框架机制应强制做复用优先分析。例如本次应先比较：
  - 方案 A：复用 `@AuthenticationResource(authorities = "ADMIN")` 注册 gateway 全局资源，admin 内部另用专属 RBAC 注解。
  - 方案 B：gateway 直接按 `/admin/**` 做配置化 authority 准入，admin 内部做 RBAC。
  - 方案 C：gateway 只做 token 解析和 `auth_user` 注入，所有 admin 权限交给 admin-service。
- 如果推荐方案会绕开或替代既有框架机制，CR 必须标记为“需要用户裁决”并停止，不得直接追加可执行实现任务。
- 任务规划中的 `Files:` 不应只列代码文件，还应要求对应模块的设计说明、边界决策和验证证据；否则实现阶段容易把任务解释为直接改代码。
- 校验器应能识别 S2 权限类 CR 是否缺少“方案比较、用户确认、模块级边界设计和复用机制分析”。

## 初步归因

- 归因结论：待观察，可抽象为上游规则候选。
- 归因说明：
  - `fons4ai-sdd-change` 技能契约要求读取代码事实、分析影响范围并生成 CR 与增量任务，但对“跨模块 S2 权限变更是否必须先输出模块级技术方案和设计 Loop”约束不足。
  - CR 模板覆盖了影响范围、技术设计影响、回归风险和增量任务，但没有强制列出“候选方案比较”“现有机制复用分析”“用户参与决策点”“被影响模块独立方案输出”。
  - 任务规划模板要求 `Files:`、`Verification:`、`Quality:`、`Done:`，但没有强制把设计确认作为实现前置 Gate，导致实现阶段可在方案未充分确认时直接编码。
  - 校验器目前能检查 CR 和任务规划结构，但未能发现“gateway 服务被影响却没有模块级设计方案”“权限边界方案未比较”“用户未参与关键边界选择”等设计质量缺口。
- 不确定项：
  - 当前仅基于一个试点项目，尚未形成多项目重复证据。
  - 是否需要新增独立 gateway 技术设计文档，还是在 CR 中增加模块级设计章节，需由上游统一规范决定。
  - `fons4ai-sdd-change` 和 `fons4ai-sdd-design` 的职责衔接边界需要进一步确认。
- 证据约束：
  - 本单不认定这是项目私有业务逻辑错误，而是记录一次 S2 change 过程中暴露出的流程与模板约束不足。
  - 本单不包含真实密钥、真实 token、数据库连接、内网地址或生产数据。

## 是否建议回流上游

- 建议回流：是。
- 判断依据：
  - 问题不只表现为某段代码错误，而是 change 阶段缺少设计 Loop 和模块级方案输出，导致实现阶段在关键权限边界未确认时直接编码。
  - 该问题可抽象为 S2 change 的通用流程约束：凡涉及权限、安全、gateway、auth、支付、消息、数据迁移等核心边界，应强制方案比较和用户裁决。
  - 当前跨项目重复性待观察，因此建议先作为上游规则候选进入 Loop Phase 1，而不是直接认定为稳定通用缺陷。
- 若不回流，应留在业务项目的处理位置：
  - 需要在当前 feature 的后续 CR 中补充权限边界方案确认，并修正 gateway 与 admin 权限注解设计。

## 建议修改位置

- skill：
  - `fons4ai-sdd-change/SKILL.md`
  - `fons4ai-sdd-implement/SKILL.md`
- template：
  - `fons4ai-sdd-change/assets/templates/change-template.md`
  - `fons4ai-sdd-tasks` 的任务规划模板或任务追加片段规则。
- script：
  - `fons4ai-sdd-tasks/scripts/validate_sdd_artifacts.py`
- docs：
  - SDD change 与设计阶段的 handoff 说明。
  - S2 权限、安全、跨核心模块变更的设计 Gate 说明。
- validator：
  - 增加 S2 change 设计充分性校验。
  - 增加跨模块影响与模块级方案输出一致性校验。
  - 增加“任务已可执行但用户关键裁决缺失”的风险提示。

## 证据清单

| 结论 | 证据来源 | 证据等级 | 状态 |
| --- | --- | --- | --- |
| CR 已识别 gateway、common-web、admin 等跨模块影响，但主要输出为控制面 CR 和任务追加 | `spec/features/20260630/changes/CR-001.md` 第 4、6、10、11 节 | L2 | 已验证 |
| 增量任务 T023-T026 被追加为可执行任务，并把 gateway 权限改造列为实现项 | `spec/features/20260630/框架控制面-任务规划.md` 第 1 节和 T023-T026 | L2 | 已验证 |
| 实施阶段已经根据 CR 进入编码并产生 gateway/admin 权限边界实现结果 | `spec/features/20260630/reports/框架控制面-实施报告.md` 2026-07-09 CR-001 执行增量 | L2 | 已验证 |
| 用户明确反馈缺少 gateway 对应技术方案、缺少设计 Loop 和边界控制参与 | 2026-07-09 用户对本次 SDD 体验的明确反馈 | L2 | 已验证 |
| `fons4ai-sdd-change` 契约要求生成 CR 和任务后停止，但未明确跨模块 S2 权限变更必须先产出模块级方案比较和用户裁决 | `C:/Users/chuang_ying_h/.agents/skills/fons4ai-sdd-change/SKILL.md` 的 Contract、变更澄清门禁和变更规划流程 | L2 | 已验证 |
| 建议回流上游，但跨项目重复性仍需观察 | 当前仅基于一个试点项目，未读取到多项目重复案例 | L1 | 待观察 |

证据等级说明：

- L1：用户体验描述、单项目观察或脱敏背景。
- L2：可定位的技能契约、CR、任务规划、实施报告或用户明确反馈。
- L3：多项目重复记录、校验器可复现失败或正式上游缺陷确认。本单暂未形成 L3。

## 脱敏说明

- 敏感信息状态：已脱敏。
- 已移除或替换的信息：
  - 未写入真实 Nacos 地址、数据库地址、Redis 地址、账号、密码、token、clientSecret、内网 IP、业务数据。
  - 来源项目仅描述为匿名框架类基础设施项目。
  - 问题描述仅保留模块类型、技能名称、文档路径和脱敏后的设计现象。
- 仍需人工确认的敏感内容：
  - 无。

## 后续行动

- 业务项目本地处理：
  - 后续应补一个新的 CR 或修正 CR-001，重新确认 gateway 准入机制是否复用 `@AuthenticationResource(authorities = "ADMIN")`，并明确 admin 内部 RBAC 与 gateway 全局资源的组合方式。
  - 在实现前应让用户确认关键权限边界方案，再进入代码修改。
- 上游 `fons4ai-engineering` 处理：
  - 建议进入 Loop Phase 1，评估是否为 S2 change 增加“设计 Loop Gate”。
  - 建议补充 change 模板中的候选方案比较、既有机制复用分析、受影响模块设计输出、用户裁决点和禁止直接实现条件。
  - 建议补充 validator，对权限、安全、gateway、auth 等关键词触发设计充分性检查。
- 观察项：
  - 其他项目中 S2 change 是否也出现“CR 快速生成、任务可执行、但关键设计边界未确认”的类似问题。
  - 是否需要将跨模块核心链路变更强制路由到 `fons4ai-sdd-design` 生成补充设计，再回到 `fons4ai-sdd-change` 追加任务。
