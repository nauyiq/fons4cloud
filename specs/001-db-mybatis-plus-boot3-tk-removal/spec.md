# Feature Specification: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除

**Feature Branch**: `001-db-mybatis-plus-boot3-tk-removal`  
**Created**: 2026-04-30  
**Status**: 补救追溯完成  
**Input**: 用户要求调研 db 模块中 `mybatis-plus-boot-starter` 替换为 `mybatis-plus-spring-boot3-starter` 的影响，并移除 TK Mapper；用户已明确允许删除 TK Mapper 依赖和 `com.fons.cloud.db.tk` 整包，并接受对外不兼容变更。  
**SDD Level**: `S2`

## SDD 等级判定

### 当前等级

S2 复杂能力变更。

### 判定依据

- **影响范围**: 影响 `fons4cloud-common-db-core` 公共 db 能力、`fons4cloud-common-skywalking` 日志配置，以及依赖该公共 db 模块的下游服务。
- **契约变化**: 删除 `com.fons.cloud.db.tk` 公共包，移除 `BaseTkMapper`、`BaseTkService`、`PrimaryLessTkMapper` 等对外可引用类型，属于明确破坏性变更。
- **数据变化**: 不涉及数据库表结构、缓存结构或 MQ 消息结构变更。
- **风险能力**: 涉及框架层数据访问 starter、MyBatis 自动配置、公共 Mapper 基类迁移路径和依赖收敛，属于数据访问基础能力变更。
- **验证方式**: Maven 依赖树验证、db-core 构建、db-shardingsphere 下游构建、源码残留扫描；自动化测试本次使用 `-DskipTests` 跳过。

### 升级条件

本次已按最高 SDD 等级 S2 处理，无需继续升级。若后续发现业务模块仍直接引用 TK Mapper 类型，应新增迁移任务并补充业务模块级验证。

## 适用规范

本变更必须遵循：

1. `.specify/memory/constitution.md`
2. `.specify/memory/java-development-standard.md`
3. `AGENTS.md`

## User Scenarios & Testing

### User Story 1 - db 模块依赖适配 Spring Boot 3 (Priority: P1)

作为框架维护者，我希望 db-core 使用面向 Spring Boot 3 的 MyBatis-Plus starter，避免继续依赖面向 Boot 2 的 starter 组合，降低 MyBatis/Spring 依赖调解风险。

**Why this priority**: 根 POM 使用 Spring Boot 3.5.8，db-core 是公共数据访问能力，starter 不匹配会提高升级和运行时风险。

**Independent Test**: 查看 db-core 依赖树，确认最终引入 `mybatis-plus-spring-boot3-starter` 且未出现旧 `mybatis-plus-boot-starter`。

**Acceptance Scenarios**:

1. **Given** 项目使用 Spring Boot 3.5.8，**When** 构建 db-core 依赖树，**Then** MyBatis-Plus starter 应为 `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5`。
2. **Given** PageHelper 仍需要 MyBatis Boot 自动配置类，**When** 替换 MyBatis-Plus starter，**Then** 保留 `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3`，避免 PageHelper 自动配置类缺失。

---

### User Story 2 - 移除 TK Mapper 公共能力 (Priority: P1)

作为框架维护者，我希望删除 TK Mapper 依赖和公共封装包，使 db 模块只保留 MyBatis 与 MyBatis-Plus 方向，减少重复数据访问抽象。

**Why this priority**: 用户已明确接受删除 TK Mapper 依赖、删除 `com.fons.cloud.db.tk` 整包和对外不兼容变更。

**Independent Test**: 源码和依赖树扫描不再出现 `tk.mybatis`、`mapper-spring-boot-starter`、`com.fons.cloud.db.tk`、`BaseTkMapper`、`BaseTkService`、`PrimaryLessTk`。

**Acceptance Scenarios**:

1. **Given** db-core 原先声明 TK Mapper starter，**When** 查看 db-core POM，**Then** 不再存在 `tk.mybatis:mapper-spring-boot-starter` 及其版本属性。
2. **Given** 业务代码可能引用 `com.fons.cloud.db.tk`，**When** 本次变更发布后，**Then** 这些调用方必须迁移到 `com.fons.cloud.db.mybatisplus` 或自有 Mapper 实现。
3. **Given** SkyWalking 日志配置存在 `tk.mybatis` logger，**When** 移除 TK Mapper，**Then** 对应 logger 配置同步删除。

---

### User Story 3 - 保持现有 MyBatis 公共能力可构建 (Priority: P2)

作为框架维护者，我希望删除 TK Mapper 后，现有 MyBatis-Plus 自动配置、通用 Mapper XML 和下游 db 模块仍能构建通过。

**Why this priority**: 破坏性删除不能影响仍保留的 MyBatis/MyBatis-Plus 能力。

**Independent Test**: 使用 JDK 21 构建 db-core 和 db-shardingsphere 下游链路。

**Acceptance Scenarios**:

1. **Given** `CreateTableSql` 不再依赖 JPA `@Column`，**When** `CommonMapper.xml` 查询 `SHOW CREATE TABLE`，**Then** 仍通过 resultMap 显式映射 `Table` 与 `Create Table` 字段。
2. **Given** 下游 `fons4cloud-common-db-shardingsphere` 依赖 db-core，**When** 运行 Maven 构建，**Then** 构建应成功。

### Edge Cases

- 业务模块仍直接继承已删除的 TK Mapper 基类：编译期失败，需迁移到 MyBatis-Plus 基类或普通 MyBatis Mapper。
- 本地 Maven 默认绑定 JDK 8：构建会失败并提示 `无效的目标发行版: 21`，需设置 `JAVA_HOME=C:\hongqy\C\Java\jdk21`。
- `CommonMapper.xml` 中 `SHOW CREATE TABLE ${tableName}` 和 `executeSql ${sql}` 属于既有动态 SQL 能力，本次不改动其行为；若后续治理 SQL 注入风险，应单独立项。

## Requirements

### Functional Requirements

- **FR-001**: db-core 必须从 `mybatis-plus-boot-starter` 切换为 `mybatis-plus-spring-boot3-starter`。
- **FR-002**: db-core 必须移除 `tk.mybatis:mapper-spring-boot-starter` 依赖和版本属性。
- **FR-003**: db-core 必须删除 `com.fons.cloud.db.tk` 整包公开类型。
- **FR-004**: 代码中不得残留对 `javax.persistence.Column` 的依赖，仅因 TK Mapper/JPA 注解而存在的引用必须清理。
- **FR-005**: 日志配置不得残留 TK Mapper logger。
- **FR-006**: 变更必须保留现有 MyBatis-Plus 自动配置能力和 `CommonMapper.xml` 显式 resultMap 映射。

### Non-Functional Requirements

- **NFR-001**: 破坏性变更必须在文档中明确标记，并记录用户已接受。
- **NFR-002**: 不新增新的数据访问抽象，不重复封装 MyBatis-Plus 已有能力。
- **NFR-003**: 必须记录依赖树、源码残留扫描和构建验证结果。
- **NFR-004**: 必须记录测试跳过事实和残余风险。

### Key Entities

- **db-core 依赖契约**: db-core 对外提供 MyBatis/MyBatis-Plus 相关依赖、自动配置和公共基类。
- **MyBatis-Plus 公共基类**: `com.fons.cloud.db.mybatisplus.BasePlusMapper`、`BasePlusService`、`BasePlusServiceImpl` 等仍保留。
- **TK Mapper 公共基类**: `com.fons.cloud.db.tk.*` 已删除，调用方需迁移。

## Success Criteria

### Measurable Outcomes

- **SC-001**: Maven 依赖树中出现 `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5`。
- **SC-002**: Maven 依赖树和源码扫描中不再出现 `tk.mybatis` 与 `mapper-spring-boot-starter`。
- **SC-003**: db-core 使用 JDK 21 执行 `mvn -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core -am -DskipTests package` 构建成功。
- **SC-004**: db-shardingsphere 下游链路使用 JDK 21 执行 `mvn -pl fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-shardingsphere -am -DskipTests package` 构建成功。

## Assumptions

- 当前项目基线为 Java 21、Spring Boot 3.5.8。
- 用户接受删除 TK Mapper 造成的源码级不兼容。
- PageHelper 1.4.1 暂不升级，仅保留当前依赖组合并通过显式 MyBatis Boot starter 满足自动配置类引用。
- 本次不迁移业务模块中潜在的外部 TK Mapper 使用方；若存在，后续由业务模块编译暴露并单独处理。

## Out of Scope

- 不升级 MyBatis-Plus 版本号。
- 不升级 PageHelper 版本号。
- 不重写现有 `CommonMapper.xml` 动态 SQL。
- 不新增运行时集成测试或数据库容器测试。
- 不处理仓库中已存在或构建产生的 `.flattened-pom.xml`、`target/` 未跟踪文件。

