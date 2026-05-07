# Feature Specification: [FEATURE NAME]

**Feature Branch**: `[###-feature-name]`  
**Created**: [DATE]  
**Status**: Draft  
**Input**: User description: "$ARGUMENTS"  
**SDD Level**: `[S0/S1/S2]`

**Note**: 生成内容必须使用中文。所有 feature 必须先判定 SDD 等级，并按等级裁剪产物。

## SDD 等级判定

### 当前等级

[填写 S0 轻量变更 / S1 标准功能 / S2 复杂能力]

### 判定依据

- **影响范围**: [涉及文件、模块、调用方数量]
- **契约变化**: [是否新增或修改公共 API、外部接口、配置契约]
- **数据变化**: [是否涉及数据库结构、缓存结构、MQ 消息、迁移]
- **风险能力**: [是否涉及认证、网关、限流、安全、事务、缓存一致性]
- **验证方式**: [单元测试、局部构建、集成测试、人工验证]

### 升级条件

若后续调研发现实际影响超过当前等级，必须升级：

- S0 发现公共 API、数据库、跨模块或高风险能力影响时，升级为 S1 或 S2。
- S1 发现跨核心模块、公共抽象、迁移、安全或兼容性风险时，升级为 S2。

## 适用规范

本功能必须遵循：

1. `.specify/memory/constitution.md`
2. `.specify/memory/java-development-standard.md`
3. `AGENTS.md`

若本功能涉及 Java 代码、框架能力、业务服务接入、数据库、缓存、MQ、认证、网关、限流或远程调用，后续 SDD 产物必须包含 Java 开发规范检查。

## S0 轻量变更记录

> 仅当 SDD Level 为 S0 时填写本节。S1/S2 可以删除本节或标记为 N/A。

### 现状事实

- **已阅读文件**: [列出相关代码、配置、测试、调用方]
- **调用链事实**: [说明相关调用关系]
- **问题证据**: [说明缺陷、现状或用户诉求对应的事实]
- **复用判断**: [说明可复用的现有能力，或无需新增抽象的原因]

### 目标与验收

- **AC-001**: Given [初始状态]，When [动作]，Then [期望结果]
- **AC-002**: Given [初始状态]，When [动作]，Then [期望结果]
- **AC-003**: Given [初始状态]，When [动作]，Then [期望结果]

### 影响范围

- **涉及模块/文件**: [1-3 个文件或明确说明]
- **是否修改既有行为**: [否/是，若是必须记录用户确认]
- **范围外事项**: [不在本次处理的内容]

### 执行清单

- [ ] 实现前补充失败测试或等价验证方式
- [ ] 完成最小实现，不引入不必要抽象
- [ ] 运行局部验证命令或执行人工验证
- [ ] 记录验证结果和残余风险

## User Scenarios & Testing

> S1/S2 必填。S0 若已有“目标与验收”且无需用户故事拆分，可标记为 N/A。

### User Story 1 - [Brief Title] (Priority: P1)

[用自然语言描述该用户旅程]

**Why this priority**: [说明价值和优先级原因]

**Independent Test**: [说明如何独立验证该故事]

**Acceptance Scenarios**:

1. **Given** [初始状态]，**When** [动作]，**Then** [期望结果]
2. **Given** [初始状态]，**When** [动作]，**Then** [期望结果]

---

### User Story 2 - [Brief Title] (Priority: P2)

[用自然语言描述该用户旅程]

**Why this priority**: [说明价值和优先级原因]

**Independent Test**: [说明如何独立验证该故事]

**Acceptance Scenarios**:

1. **Given** [初始状态]，**When** [动作]，**Then** [期望结果]

---

### Edge Cases

- [边界条件：空值、非法输入、重复请求、并发、超时、权限、租户、兼容性]
- [失败场景：外部服务失败、缓存异常、数据库失败、MQ 失败、鉴权失败]
- [安全场景：敏感字段、token/clientSecret/accessSecret、日志脱敏]

## Requirements

### Functional Requirements

- **FR-001**: 系统必须 [具体可观察能力]
- **FR-002**: 系统必须 [输入校验、失败处理或边界行为]
- **FR-003**: 系统必须 [复用现有 fons4cloud 模块或公共能力]
- **FR-004**: 系统必须 [记录异常、日志、安全或审计预期]
- **FR-005**: 若涉及 Java 代码，系统必须符合 `.specify/memory/java-development-standard.md`。

### Non-Functional Requirements

- **NFR-001**: 必须保持现有兼容性；任何破坏性变更必须明确标记并获得确认。
- **NFR-002**: 必须避免重复造轮子，新增抽象、依赖或模块必须说明必要性。
- **NFR-003**: 必须记录测试、验证命令或人工验证步骤。

### Key Entities *(include if feature involves data)*

- **[Entity 1]**: [实体含义、关键属性、关系和约束，不写实现细节]
- **[Entity 2]**: [实体含义、关键属性、关系和约束，不写实现细节]

## Success Criteria

### Measurable Outcomes

- **SC-001**: [可衡量结果，例如某接口在指定输入下返回指定响应]
- **SC-002**: [兼容性或性能结果]
- **SC-003**: [测试或验证结果]
- **SC-004**: [用户价值或业务结果]

## Assumptions

- [默认假设，例如复用现有认证体系]
- [范围边界，例如不包含历史数据迁移]
- [环境或依赖假设，例如 Redis/MySQL/Dubbo 可用]

## Out of Scope

- [明确不在本次实现范围内的事项]
