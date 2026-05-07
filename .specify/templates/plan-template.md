# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]  
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`  
**SDD Level**: `[S1/S2]`

**Note**: This template is filled in by the `/speckit.plan` command. 生成内容必须使用中文。S0 轻量变更不强制生成 `plan.md`；若已升级为 S1/S2，必须填写本文件。

## Summary

[从 feature spec 提取：核心需求、目标用户、主要技术路径和交付边界]

## 适用规范

本功能必须遵循：

1. `.specify/memory/constitution.md`
2. `.specify/memory/java-development-standard.md`
3. `AGENTS.md`
4. 当前模块既有代码风格和调用链事实

若规范与现有局部代码冲突，优先级为：

1. 不破坏现有兼容性和已发布 API。
2. 遵循 Java 开发规范。
3. 保持当前模块局部一致性。
4. 在本 plan 中记录冲突、取舍和后续治理建议。

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.5.8, Spring Cloud 2025.0.0, Spring Cloud Alibaba 2025.0.0.0, Dubbo 3.3.0  
**Storage**: [MySQL/MyBatis-Plus/Redis/MQ/文件/N/A]  
**Testing**: [JUnit/Spring Boot Test/Mapper 测试/集成测试/人工验证]  
**Target Platform**: [Linux 服务端/框架模块/业务服务/N/A]  
**Project Type**: Maven 多模块 Java 框架项目  
**Performance Goals**: [领域指标或 N/A]  
**Constraints**: [兼容性、安全、事务、缓存、限流、迁移约束或 N/A]  
**Scale/Scope**: [影响模块、接口数量、数据范围、调用方或 N/A]

## 调研证据

实现前必须列出已阅读的事实来源：

- [ ] 相关 Java 类：
- [ ] 相关 Mapper XML：
- [ ] 相关 POM/配置：
- [ ] 相关接口/调用方：
- [ ] 相关测试/验证方式：
- [ ] 现有公共能力复用点：

## 分级门禁

- [ ] **等级匹配**：当前等级为 S1 或 S2，且判定依据与 `spec.md` 一致。
- [ ] **升级检查**：已确认不存在需要升级到更高等级的事实；若存在，已完成升级。
- [ ] **用户确认**：若删除或修改既有行为，已获得用户明确确认。

## Constitution Check

- [ ] **简洁性**：方案是满足当前需求的最简单可维护实现；新增抽象、依赖、模块或配置均有必要性说明。
- [ ] **事实调研**：关键设计判断可追溯到已阅读代码、配置、接口、测试或用户确认。
- [ ] **模块边界**：变更位于正确模块；公共能力进入 `fons4cloud-common`，认证进入 `fons4cloud-auth`，网关进入 `fons4cloud-gateway`，消息进入 `fons4cloud-mq`，starter 进入 `fons4cloud-starter`。
- [ ] **复用优先**：已确认没有重复实现已有工具函数、组件、starter、缓存、锁、认证、限流、MQ 或数据访问能力。
- [ ] **TDD/验证**：已定义实现前会失败的测试或等价验证方式。
- [ ] **中文交付**：规格、计划、任务、检查清单和项目文档使用中文。

## Java 开发规范检查

- [ ] 已阅读 `.specify/memory/java-development-standard.md`。
- [ ] Spring Bean 使用构造注入，优先 `@RequiredArgsConstructor` + `final` 字段。
- [ ] 对外接口统一返回 `R<T>` 或项目既有响应模型。
- [ ] 业务异常使用 `BizException` + `ResultCode` / `XxxResultCode`。
- [ ] Entity 不直接暴露给外部接口，使用 Converter 转换。
- [ ] Mapper XML 避免 `SELECT *`，`AND` / `OR` 混用加括号。
- [ ] 日志使用参数化写法，不输出敏感信息。
- [ ] 安全、缓存、事务、幂等和远程调用风险已说明。
- [ ] 受影响模块测试或等价验证命令已记录。

## 技术方案

### 复用与放置

- **复用能力**: [已有工具函数、组件、starter、公共模块或基础设施]
- **变更位置**: [实际涉及模块和路径]
- **不新增抽象的理由**: [若适用]

### 接口、数据与集成

- **API/契约**: [新增、修改或 N/A]
- **数据库/缓存/MQ**: [新增、修改或 N/A]
- **事务与一致性**: [策略或 N/A]
- **安全与权限**: [策略或 N/A]

### 兼容性与风险

- **兼容性影响**: [无/有，说明影响和处理方式]
- **迁移/回滚**: [S2 必填；S1 可填 N/A]
- **残余风险**: [风险、原因、验证方式]

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── spec.md
├── plan.md
├── tasks.md
├── research.md        # S2 按需
├── data-model.md      # S2 涉及数据模型时
├── quickstart.md      # S2 验收流程复杂时
├── contracts/         # S2 涉及外部契约时
└── checklists/        # S2 需要独立检查清单时
```

### Source Code

```text
[列出本次实际涉及的模块和路径，例如：]
fons4cloud-auth/
fons4cloud-common/
fons4cloud-gateway/
fons4cloud-mq/
fons4cloud-starter/
```

**Structure Decision**: [说明变更放置位置和原因]

## Complexity Tracking

> S2 必填。S1 仅当存在宪章或 Java 规范门禁无法直接满足时填写。每项复杂度必须说明必要性和被拒绝的更简单方案。

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [N/A] | [N/A] | [N/A] |

## 验证策略

- **Red**: [实现前会失败的测试或等价验证]
- **Green**: [最小实现后的验证命令]
- **Refactor**: [重构和合规检查方式]
- **人工验证**: [无法自动化时填写步骤和风险原因]

## Phase 0: Research

> S2 必填；S1 可在本节简要记录关键选择，不强制生成独立 `research.md`。

[记录关键技术选择、现状约束、可复用能力、风险和待确认事项]

## Phase 1: Design

> S2 按需生成 `data-model.md`、`contracts/`、`quickstart.md`；S1 优先在本文件内完成设计说明。

[记录 API、数据库、缓存、事务、异常码、日志、安全、兼容性和测试设计]

## Phase 2: Task Planning

[说明 tasks.md 的拆分策略：Red 测试、Green 实现、Refactor 验证、文档同步]
