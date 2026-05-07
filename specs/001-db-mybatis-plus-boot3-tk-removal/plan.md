# Implementation Plan: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除

**Branch**: `001-db-mybatis-plus-boot3-tk-removal` | **Date**: 2026-04-30 | **Spec**: `spec.md`  
**Input**: Feature specification from `/specs/001-db-mybatis-plus-boot3-tk-removal/spec.md`  
**SDD Level**: `S2`

## Summary

将 db-core 的 MyBatis-Plus starter 从 Boot 2 风格的 `mybatis-plus-boot-starter` 替换为 Boot 3 风格的 `mybatis-plus-spring-boot3-starter`，删除 TK Mapper 依赖与 `com.fons.cloud.db.tk` 公共包，并清理随之产生的 JPA 注解和日志配置残留。该变更是框架层公共数据访问能力的破坏性收敛，用户已明确接受不兼容影响。

## 适用规范

本功能必须遵循：

1. `.specify/memory/constitution.md`
2. `.specify/memory/java-development-standard.md`
3. `AGENTS.md`
4. 当前 db-core 代码、Mapper XML、POM、日志配置和 Maven 依赖树事实

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.5.8, MyBatis Spring Boot Starter 3.0.3, MyBatis-Plus 3.5.5, PageHelper Spring Boot Starter 1.4.1  
**Storage**: MySQL / MyBatis / MyBatis-Plus  
**Testing**: Maven 构建、依赖树扫描、源码残留扫描；自动化测试本次跳过  
**Target Platform**: Java 框架公共模块  
**Project Type**: Maven 多模块 Java 框架项目  
**Performance Goals**: N/A，本次不改变运行时查询逻辑  
**Constraints**: 保留 PageHelper 可用性；删除 TK Mapper 属于破坏性变更；不扩大到 PageHelper 升级  
**Scale/Scope**: `fons4cloud-common-db-core`、`fons4cloud-common-db-datasource`、`fons4cloud-common-db-shardingsphere` 构建链路、`fons4cloud-common-skywalking` 日志配置

## 调研证据

- [x] 相关 Java 类：`MybatisPlusAutoConfiguration`、`CreateTableSql`、`CommonMapper`、`BasePlusMapper`
- [x] 相关 Mapper XML：`CommonMapper.xml`
- [x] 相关 POM/配置：根 `pom.xml`、db-core POM、db-datasource POM、db-shardingsphere POM、logback.xml
- [x] 相关接口/调用方：db-core 自动配置、MyBatis-Plus 基类、CommonDbService
- [x] 相关测试/验证方式：Maven package、dependency:tree、源码残留扫描
- [x] 现有公共能力复用点：`com.fons.cloud.db.mybatisplus` 包下 MyBatis-Plus 封装

## 分级门禁

- [x] **等级匹配**: 当前等级为 S2，与 `spec.md` 判定一致。
- [x] **升级检查**: 已按最高等级 S2 补救，无需继续升级。
- [x] **用户确认**: 用户已明确允许删除 TK Mapper 依赖和 `com.fons.cloud.db.tk` 整包，并接受对外不兼容变更。

## Constitution Check

- [x] **简洁性**: 不新增兼容层，不新增替代抽象，仅收敛到已有 MyBatis-Plus 公共封装。
- [x] **事实调研**: 关键判断来自 POM、依赖树、自动配置、Mapper XML 和用户确认。
- [x] **模块边界**: 公共数据访问能力保留在 `fons4cloud-common-db-core`。
- [x] **复用优先**: 迁移方向复用 `com.fons.cloud.db.mybatisplus`，不重复实现 CRUD 抽象。
- [x] **TDD/验证**: 由于本次为依赖和公共 API 删除，采用依赖树、构建和残留扫描作为等价验证；未新增自动化测试。
- [x] **中文交付**: SDD 文档使用中文。

## Java 开发规范检查

- [x] 已读取 `.specify/memory/java-development-standard.md`。
- [x] 未新增 Spring Bean 注入逻辑。
- [x] 未新增对外 HTTP/RPC 接口。
- [x] 未新增业务异常。
- [x] 未新增 Entity 暴露。
- [x] `CommonMapper.xml` 既有 resultMap 明确映射 `CreateTableSql` 字段。
- [x] 日志配置移除 TK Mapper logger，不新增敏感信息输出。
- [x] 数据访问依赖、兼容性和残余风险已记录。
- [x] 受影响模块构建命令已记录。

## 技术方案

### 复用与放置

- **复用能力**: 继续使用 `com.fons.cloud.db.mybatisplus` 下的 `BasePlusMapper`、`BasePlusService`、`BasePlusServiceImpl` 和 MyBatis-Plus 自动配置。
- **变更位置**:
  - `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml`
  - `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/java/com/fons/cloud/db/tk`
  - `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/java/com/fons/cloud/db/common/CreateTableSql.java`
  - `fons4cloud-common/fons4cloud-common-skywalking/src/main/resources/logback.xml`
- **不新增抽象的理由**: 用户目标是移除 TK Mapper；保留兼容层会继续暴露旧 API，与目标冲突。

### 接口、数据与集成

- **API/契约**: 删除 `com.fons.cloud.db.tk.*` 属于源码级公共契约删除。
- **数据库/缓存/MQ**: 无结构变更。
- **事务与一致性**: N/A，本次不改变事务边界。
- **安全与权限**: N/A，本次不改变认证、授权或敏感信息处理。

### 兼容性与风险

- **兼容性影响**: 引用 TK Mapper 公共类型的模块会编译失败，需要迁移到 MyBatis-Plus 或普通 MyBatis Mapper。
- **迁移/回滚**:
  - 迁移：将 `BaseTkMapper`、`BaseTkService` 相关继承关系替换为 `BasePlusMapper`、`BasePlusService`，或改为普通 MyBatis Mapper。
  - 回滚：恢复 TK Mapper starter、`com.fons.cloud.db.tk` 包和 `tk.mybatis` logger；不推荐，除非下游短期无法迁移。
- **残余风险**: 未运行全量测试；若业务模块未纳入本次构建，仍可能存在 TK Mapper 引用。

## Project Structure

### Documentation

```text
specs/001-db-mybatis-plus-boot3-tk-removal/
|-- spec.md
|-- plan.md
|-- research.md
|-- tasks.md
|-- quickstart.md
|-- contracts/
|   `-- compatibility.md
`-- checklists/
    `-- sdd-gate.md
```

### Source Code

```text
fons4cloud-common/
|-- fons4cloud-common-db/
|   |-- fons4cloud-common-db-core/
|   |-- fons4cloud-common-db-datasource/
|   `-- fons4cloud-common-db-shardingsphere/
`-- fons4cloud-common-skywalking/
```

**Structure Decision**: 依赖和公共数据访问能力属于 `fons4cloud-common-db-core`；日志残留属于 `fons4cloud-common-skywalking` 资源配置。

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| 删除公共 API 造成不兼容 | 用户明确要求移除 TK Mapper 依赖和整包 | 保留兼容层会继续依赖 TK Mapper，无法达成本次目标 |
| SDD 补救追溯而非事前推进 | 前一轮执行已越过 SDD 审核步骤，需要补齐可追溯记录 | 不补文档会继续违反项目治理要求 |

## 验证策略

- **Red**: 依赖树和源码扫描在变更前可发现 `tk.mybatis`、`mapper-spring-boot-starter`、`mybatis-plus-boot-starter`、`com.fons.cloud.db.tk`。
- **Green**: 替换依赖、删除 TK Mapper 包和残留配置后，db-core 构建通过，依赖树无 TK Mapper。
- **Refactor**: 清理 `CreateTableSql` 上仅因 JPA/TK 依赖存在的 `javax.persistence.Column` 注解；清理 SkyWalking logback 中的 TK logger。
- **人工验证**: 记录 JDK 21 环境要求、Maven 构建命令和跳过测试风险。

## Phase 0: Research

详见 `research.md`。关键结论是：Boot 3 基线应使用 `mybatis-plus-spring-boot3-starter`；PageHelper 当前版本要求保留显式 MyBatis Boot starter；TK Mapper 可在用户接受不兼容的前提下删除。

## Phase 1: Design

本次不新增数据库模型、HTTP/RPC contract 或配置属性。设计重点是依赖收敛和兼容性声明：

- POM 删除 TK Mapper starter 和版本属性。
- POM 替换 MyBatis-Plus starter artifactId。
- 删除 `com.fons.cloud.db.tk` 包。
- 删除 `CreateTableSql` 的 JPA 注解依赖。
- 删除 `logback.xml` 中 `tk.mybatis` logger。
- 保留 MyBatis Boot starter 与 PageHelper 现状。

## Phase 2: Task Planning

详见 `tasks.md`。任务按 Gate、Red、Green、Refactor、Verify 拆分，并标注本次为补救追溯状态。

