<!--
Sync Impact Report
Version change: 1.1.0 -> 1.2.0
Modified principles:
- III. 渐进式规格与审核闭环：补充 S0/S1/S2 分级 SDD 流程
Modified sections:
- SDD 产物强制引用 -> SDD 分级产物治理
- 开发流程与质量门禁：补充分级执行规则
Templates requiring updates:
- updated: .specify/templates/spec-template.md
- updated: .specify/templates/plan-template.md
- updated: .specify/templates/tasks-template.md
- updated: .specify/templates/checklist-template.md
- updated: .specify/workflows/speckit/workflow.yml
Follow-up TODOs:
- 无
-->

# fons4cloud Constitution

## Core Principles

### I. 简洁可维护优先

所有设计和实现必须优先选择能够满足当前需求的最简单可维护方案。新增抽象、框架、模块、配置层或通用能力时，必须说明当前问题、必要性，以及被拒绝的更简单替代方案。不得为了预期中的未来需求引入尚无事实支撑的防御性设计。

### II. 事实调研先行

任何设计、修改或删除代码前，必须先阅读相关文件、配置、调用方和现有实现，以仓库事实作为判断依据。不得仅凭记忆、命名猜测或框架习惯做出实现决策。若发现用户描述与代码事实不一致，必须坦率指出并基于事实修正方案。

### III. 渐进式规格与审核闭环

功能工作必须遵循“构思方案 -> 提请审核 -> 分解任务 -> 执行验证”的顺序。需求、边界、验收标准或影响范围仍有关键疑点时，不得进入编码实现。

所有变更必须先分级为 S0、S1 或 S2，并按等级裁剪 SDD 产物：

1. **S0 轻量变更**：小修、小优化、小范围 bugfix。允许使用单文档轻量流，只保留事实调研、验收标准、影响范围、执行清单和验证记录。
2. **S1 标准功能**：常规功能新增或调整。使用 `spec.md`、`plan.md`、`tasks.md` 三文档流程，检查清单内容合并到计划和任务中。
3. **S2 复杂能力**：跨模块、公共能力、接口契约、数据库、缓存、MQ、认证、网关、限流、安全、事务或迁移相关变更。使用完整 SDD 流程，并按需生成 `research.md`、`data-model.md`、`contracts/`、`quickstart.md` 和独立 `checklist.md`。

每次迭代必须保持可独立理解、可独立验证，并清晰记录默认假设、范围外事项和升级条件。若调研发现当前等级不足以覆盖风险，必须升级到更高等级后继续。

### IV. 复用现有架构与模块边界

实现必须优先复用项目已有工具函数、组件、自动配置、公共模块和集成方式。公共能力优先沉淀在 `fons4cloud-common`，认证能力归属 `fons4cloud-auth`，网关能力归属 `fons4cloud-gateway`，消息能力归属 `fons4cloud-mq`，starter 能力归属 `fons4cloud-starter`。不得复制已有能力、绕过既有扩展点，或让职责跨模块漂移。

### V. 测试先行与可验证质量

任何行为变更必须先定义验收标准，并在实现前编写能够失败的测试或等价的可验证检查。实现必须遵循 Red-Green-Refactor：先证明缺口存在，再实现最小可行修复，最后清理重复和复杂度。无法自动化测试的场景必须记录人工验证步骤和风险原因。

## Java 开发规范治理

本项目的框架层和业务层 Java 代码必须遵循 `.specify/memory/java-development-standard.md`。该文件是 Java 编码、重构、代码评审、AI 生成代码和业务接入时的强制实施细则。

任何涉及 Java 的 feature、bugfix、重构或公共能力建设，必须在相应 SDD 产物中明确记录已读取 Java 开发规范，并保留 Java 规范合规检查。若现有局部代码风格与 Java 开发规范冲突，处理优先级为：

1. 不破坏现有兼容性和已发布 API。
2. 遵循 `.specify/memory/java-development-standard.md`。
3. 保持当前模块局部一致性。
4. 在 SDD 产物中记录冲突、取舍和后续治理建议。

## 技术与架构约束

本项目是 Java 21 + Maven 多模块的 Spring Cloud 框架项目。默认技术栈为 Spring Boot 3.5.8、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0、Dubbo 3.3.0，并使用 Maven 统一管理依赖版本和模块构建。

新增功能必须放入与职责匹配的现有模块，优先复用父 POM 的依赖管理、已有 starter、已有公共包和现有配置模式。引入新依赖、跨模块 API、公共抽象或持久化结构时，必须在技术方案中说明兼容性、测试策略和迁移影响。

安全、限流、认证、数据访问、消息、缓存、锁、任务调度、链路追踪等横切能力必须优先复用已有模块。不得在业务模块中重复实现已有基础能力。

## SDD 分级产物治理

每个 feature 的 SDD 产物必须显式引用本宪章、Java 开发规范和当前 SDD 等级。

### S0 轻量变更

适用条件：

1. 影响 1-3 个文件，且不新增模块、不改公共 API、不改数据库结构。
2. 不改变对外契约，或只是修复明确错误。
3. 风险可通过单元测试、局部构建或明确人工验证覆盖。
4. 不涉及安全、认证、网关、缓存一致性、MQ、事务边界等高风险能力。

产物要求：

1. 使用 `specs/[编号-名称]/spec.md`。
2. 不强制生成 `plan.md`、`tasks.md`、`checklist.md`。
3. `spec.md` 必须包含现状事实、目标与验收、影响范围、执行清单和验证记录。

门禁要求：

1. 若要修改或删除既有行为，必须先获得用户确认。
2. 若调研发现实际影响超过 S0 条件，必须升级到 S1 或 S2。

### S1 标准功能

适用条件：

1. 新增或调整一个清晰功能点。
2. 影响单个模块或少量协作模块。
3. 可能新增接口、配置、错误码、测试，但不涉及复杂跨模块治理。
4. 需要任务拆分，但不需要完整架构设计文档。

产物要求：

1. `spec.md` 聚焦用户目标、验收标准、边界和范围外事项。
2. `plan.md` 聚合技术方案、调研证据、复用点、风险和验证策略。
3. `tasks.md` 按 Red-Green-Refactor 拆分到可执行任务。
4. 不默认生成独立 `checklist.md`，检查项合并到 `plan.md` 和 `tasks.md`。

门禁要求：

1. `spec.md` 审核后才能写 `plan.md`。
2. `plan.md` 审核后才能写 `tasks.md`。
3. `tasks.md` 完成后才能实施。
4. 每个用户故事必须能独立验收。

### S2 复杂能力

适用条件：

1. 跨多个核心模块，例如 `fons4cloud-common`、`fons4cloud-auth`、`fons4cloud-gateway`、`fons4cloud-mq`、`fons4cloud-starter`。
2. 新增公共抽象、starter、基础设施能力或平台级规范。
3. 涉及数据库结构、迁移、缓存一致性、事务边界、MQ、认证、网关、限流、安全策略。
4. 存在兼容性风险、迁移风险、性能目标或多个调用方。

产物要求：

1. 保留完整 Spec Kit 流程：`spec.md`、`plan.md`、`tasks.md`、独立 `checklist.md`。
2. 按实际需要生成 `research.md`、`data-model.md`、`contracts/`、`quickstart.md`。
3. 必须显式通过宪章检查和 Java 规范检查。
4. 必须记录被拒绝的更简单方案、兼容性、迁移、回滚或人工验证策略。

## 开发流程与质量门禁

所有回复、规格、任务清单、治理文档和项目内新增说明必须使用中文。代码风格遵循现有代码、`AGENTS.md`、本宪章、Java 开发规范和项目内 Maven/Spring 约定。

修改代码前必须阅读相关上下文。删除或修改既有代码前必须先向用户确认，除非用户的当前请求已经明确要求该删除或修改。遇到工作树中已有未提交改动时，必须保留并绕开无关改动；若相关改动影响实现，应在理解后协同处理，不得回滚。

## Governance

本宪章优先于普通开发习惯、模板默认文档和临时口头约定。任何 Spec Kit 产物、开发任务和代码评审都必须检查是否符合本宪章。

修订宪章必须记录变更原因、影响范围、同步模板状态和版本号。原则重定义、删除或破坏既有治理语义时提升 MAJOR；新增原则、章节或显著扩展治理要求时提升 MINOR；措辞澄清、错别字和非语义修正时提升 PATCH。

宪章修订后必须同步检查 `.specify/templates` 下的 plan、spec、tasks、checklist 模板，以及实际存在的命令模板或运行时指导文档。若有文件暂缓同步，必须在 Sync Impact Report 中说明原因和后续事项。

**Version**: 1.2.0 | **Ratified**: 2026-04-21 | **Last Amended**: 2026-04-22
