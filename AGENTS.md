# AGENTS.md

<!-- fons4ai-skill-routing: enabled -->

> 适用范围：本仓库
> 知识状态：基线已建立
> 更新日期：2026-07-08

# 项目简介

fons4cloud 是面向 Java 微服务体系的云基础能力框架，提供认证鉴权、网关、消息队列、公共组件、Starter 接入和运行治理能力。

## 快速导航

| 你想做什么 | 去哪里看 | 状态 |
| --- | --- | --- |
| 查看知识库入口 | `.specify/memory/index.md` | 已建立 |
| 查看项目技术能力总览 | `.specify/memory/项目技术能力架构文档.md` | 已建立 |
| 查看项目运行总览 | `.specify/memory/项目运行架构文档.md` | 已建立 |
| 查看项目配置与资源总览 | `.specify/memory/项目配置与资源架构文档.md` | 已建立 |
| 查看技术能力知识 | `.specify/memory/capabilities/{capability-slug}/` | 按需建模 |
| 查看项目规则 | `.specify/rules/` | 可选生成 |
| 查看 SDD 产物 | `spec/features/{yyyymmdd}/` | 按需生成 |
| 查看 BUG 修复报告 | `spec/bugfixes/{yyyymmdd}/{bug中文名}-BUG修复报告.md` | 按需生成 |

## 硬性规则

- 默认使用中文沟通、编写文档和交付说明。
- 修改代码或文档前，必须先读取相关上下文。
- 不得凭空猜测业务逻辑、接口、字段、表结构或第三方 API。
- 优先遵循项目事实、知识库、规则文件和已有代码风格。
- 删除文件、覆盖文档、大范围重构、修改数据库结构前必须确认。
- 用户提供文档与源码不一致时，必须标记冲突并请求确认。
- 本仓库按技术框架/基础设施项目建模；本项目不抽象业务领域。

## 知识库建设工作流

| 阶段 | 推荐技能 | 目的 |
| --- | --- | --- |
| 项目知识基线初始化 | `fons4ai-knowledge-bootstrap` | 建立项目级知识入口、技术能力候选、核心能力和项目级架构文档 |
| 领域/能力域知识建模 | `fons4ai-domain-knowledge-modeling` | 深挖单个或多个技术能力域，沉淀接入方式、适配方案、差异规则、技术落地和资源生命周期 |
| 知识汇总治理 | `fons4ai-knowledge-summary` | 把已验证变更同步到长期知识库 |

## Fons4AI 技能路由

| 场景 | 推荐技能 |
| --- | --- |
| 正常新需求开发 | `fons4ai-sdd-feature-workflow` |
| 需求澄清/补需求说明书 | `fons4ai-sdd-requirements` |
| 技术设计补充 | `fons4ai-sdd-design` |
| 任务规划补充 | `fons4ai-sdd-tasks` |
| 用户确认执行 SDD 任务 | `fons4ai-sdd-implement` |
| SDD 实现或验证无法关闭 | `fons4ai-sdd-recovery` |
| 已有功能迭代 | `fons4ai-sdd-change` |
| BUG、异常、回归失败 | `fons4ai-bugfix-workflow` |
| 项目知识基线初始化 | `fons4ai-knowledge-bootstrap` |
| 技术能力域知识深度建模 | `fons4ai-domain-knowledge-modeling` |
| 汇总已验证知识 | `fons4ai-knowledge-summary` |
| 生成项目规则 | `fons4ai-generate-project-rules` |

无法判断场景时，先询问用户要执行哪类工作流，并给出推荐理由。

## SDD 规范

- SDD 只使用 `S1` 和 `S2`。
- 全新需求产物默认位于 `spec/features/{yyyymmdd}/`，核心文件为 `{功能中文名}-需求说明书.md`、`{功能中文名}-技术设计说明书.md`、`{功能中文名}-任务规划.md`。
- 正常新需求开发优先使用 `fons4ai-sdd-feature-workflow` 编排需求、设计和任务规划。
- 若关键需求含义、技术术语、数据语义、验收口径、兼容性、安全权限、迁移回滚或 SDD 等级存在歧义，必须先澄清，不得直接生成正式三件套。
- `fons4ai-sdd-feature-workflow`、`fons4ai-sdd-requirements`、`fons4ai-sdd-design`、`fons4ai-sdd-tasks`、`fons4ai-sdd-change` 只能生成或更新 SDD 产物，不得写业务代码。
- 生成 `{功能中文名}-任务规划.md` 或 CR 增量任务后必须暂停，等待用户确认执行。
- 用户回复 `执行`、`开始实现`、`继续执行` 时默认执行全部未完成任务；用户回复 `执行 T001,T002` 时只执行指定任务。
- 实现或验证阶段无法关闭时，用户可回复 `继续闭环`、`处理阻塞` 或 `恢复执行`，由 `fons4ai-sdd-recovery` 诊断并路由到任务、设计、变更、实现或外部 Gate。

## 知识库建设确认门禁

- 项目类型、能力域中文名、能力域 slug、职责边界、核心平台能力和标准接入/初始化/运行链路判定必须经过确认。
- 正式知识库生成前，必须先在对话中逐个提出阻塞性确认问题；默认一次只问一个问题，不得只把问题写入文件。
- 每个阻塞性问题必须包含推荐答案、推荐理由、可选方案和影响范围。
- 用户确认当前问题后，才能继续下一个阻塞问题；没有剩余阻塞问题后才能生成正式知识库。
- 用户未确认前，只能生成候选理解、盘点清单或草案，不得写成已验证知识。
- 单个实现方、单个配置样例、单个模块实现或单个部署方式不得被写成标准流程，除非经过横向对比或用户确认。
- 用户针对当前问题回复“按推荐”或“确认”，只表示当前问题已确认；除非用户明确说“全部按推荐”，否则不得视为后续所有确认问题都已确认。
