# Tasks: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除

**Input**: Design documents from `/specs/001-db-mybatis-plus-boot3-tk-removal/`  
**Prerequisites**: `spec.md`, `plan.md`, `research.md`  
**SDD Level**: `S2`  
**状态说明**: 本文件为补救追溯任务清单。代码变更已先于 SDD 文档完成，已完成项以 `[x]` 标记；未完成项记录为残余风险或后续建议。

## 实施前门禁

- [x] T001 [Gate] 对照 `.specify/memory/constitution.md` 确认本变更应归类为 S2。
- [x] T002 [Gate] 对照 `.specify/memory/java-development-standard.md` 确认 Java 与 MyBatis 相关规范适用。
- [x] T003 [Gate] 阅读 db-core POM、自动配置、Mapper XML、公共基类、日志配置和下游 db 模块 POM。
- [x] T004 [Gate] 确认用户已明确允许删除 TK Mapper 依赖和 `com.fons.cloud.db.tk` 整包，并接受不兼容变更。
- [x] T005 [Gate] 记录本次前置流程遗漏：未在编码前完成 SDD 等级判定、方案审核和任务拆分。

## Phase 1: Red - 变更前可失败验证

- [x] T006 [Red] 在 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml` 确认原依赖存在 `tk.mybatis:mapper-spring-boot-starter`。
- [x] T007 [Red] 在 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml` 确认原依赖存在 `com.baomidou:mybatis-plus-boot-starter`。
- [x] T008 [Red] 在源码中确认原先存在 `com.fons.cloud.db.tk` 包和 `javax.persistence.Column` 注解依赖。
- [x] T009 [Red] 在 `fons4cloud-common/fons4cloud-common-skywalking/src/main/resources/logback.xml` 确认原先存在 `tk.mybatis` logger。

## Phase 2: Green - 最小实现

- [x] T010 [US1] 在 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml` 删除 `tk.mybatis.version` 属性。
- [x] T011 [US1] 在 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml` 删除 `tk.mybatis:mapper-spring-boot-starter` 依赖。
- [x] T012 [US1] 在 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml` 将 `mybatis-plus-boot-starter` 替换为 `mybatis-plus-spring-boot3-starter`。
- [x] T013 [US1] 保留 `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3`，避免 PageHelper 自动配置类缺失风险。
- [x] T014 [US2] 删除 `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/java/com/fons/cloud/db/tk` 整包。

## Phase 3: Refactor - 残留清理

- [x] T015 [US3] 在 `CreateTableSql.java` 删除 `javax.persistence.Column` import 和字段注解。
- [x] T016 [US3] 通过 `CommonMapper.xml` 的 resultMap 确认 `Table` 与 `Create Table` 字段仍有显式映射。
- [x] T017 [US2] 在 `logback.xml` 删除 `tk.mybatis` logger。
- [x] T018 [US2] 扫描源码确认无 `tk.mybatis`、`com.fons.cloud.db.tk`、`BaseTkMapper`、`BaseTkService`、`PrimaryLessTk` 残留。

## Phase 4: Verify - 构建与依赖验证

- [x] T019 [Verify] 运行 db-core 构建前发现默认 Maven 使用 JDK 8，错误为 `无效的目标发行版: 21`。
- [x] T020 [Verify] 临时设置 `JAVA_HOME=C:\hongqy\C\Java\jdk21` 后运行 db-core 构建。
- [x] T021 [Verify] 运行 `C:\hongqy\tool\apache-maven-3.9.8\bin\mvn.cmd -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core -am -DskipTests package`，结果成功。
- [x] T022 [Verify] 运行依赖树过滤，确认出现 `mybatis-plus-spring-boot3-starter:3.5.5`，未出现 `tk.mybatis`。
- [x] T023 [Verify] 运行 `C:\hongqy\tool\apache-maven-3.9.8\bin\mvn.cmd -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-shardingsphere -am -DskipTests package`，结果成功。
- [x] T024 [Verify] 记录测试使用 `-DskipTests` 跳过，未完成单元/集成测试执行。

## Phase 5: SDD 补救交付

- [x] T025 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/spec.md`。
- [x] T026 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/research.md`。
- [x] T027 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/plan.md`。
- [x] T028 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/tasks.md`。
- [x] T029 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/contracts/compatibility.md`。
- [x] T030 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/quickstart.md`。
- [x] T031 [Docs] 新增 `specs/001-db-mybatis-plus-boot3-tk-removal/checklists/sdd-gate.md`。

## 后续建议

- [ ] T032 [Follow-up] 在业务服务模块中执行全量编译，确认是否仍有外部 TK Mapper 引用。
- [ ] T033 [Follow-up] 在可用测试环境中执行受影响模块测试，补足本次 `-DskipTests` 的覆盖缺口。
- [ ] T034 [Follow-up] 如确认 PageHelper 1.4.1 与 Boot 3 存在运行时风险，单独立项评估 PageHelper 升级。

## Dependencies & Execution Order

1. Gate 任务必须先完成。
2. Red 验证用于证明变更必要性。
3. Green 任务完成依赖替换和包删除。
4. Refactor 清理残留。
5. Verify 记录构建、依赖树和扫描结果。
6. Docs 任务完成本次 SDD 补救追溯。

