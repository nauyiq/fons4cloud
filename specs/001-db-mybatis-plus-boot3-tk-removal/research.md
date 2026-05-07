# Research: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除

**日期**: 2026-04-30  
**SDD Level**: S2  
**性质**: 补救追溯文档

## 调研目标

确认 db-core 在 Spring Boot 3.5.8 基线下，将 `mybatis-plus-boot-starter` 替换为 `mybatis-plus-spring-boot3-starter` 的必要性、影响范围，以及删除 TK Mapper 的可接受边界。

## 已读取事实来源

- `.specify/memory/constitution.md`
- `.specify/memory/java-development-standard.md`
- `AGENTS.md`
- `pom.xml`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/pom.xml`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/java/com/fons/cloud/db/autoconfigure/MybatisPlusAutoConfiguration.java`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/java/com/fons/cloud/db/common/CreateTableSql.java`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-core/src/main/resources/mapper/CommonMapper.xml`
- `fons4cloud-common/fons4cloud-common-skywalking/src/main/resources/logback.xml`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-datasource/pom.xml`
- `fons4cloud-common/fons4cloud-common-db/fons4cloud-common-db-shardingsphere/pom.xml`

## 关键发现

### F-001: 项目基线是 Spring Boot 3

根 POM 使用 Spring Boot `3.5.8` 和 Java 21。继续使用面向 Boot 2 的 `mybatis-plus-boot-starter` 不符合当前基线。

### F-002: MyBatis-Plus Boot3 starter 更匹配当前依赖体系

`mybatis-plus-boot-starter:3.5.5` 面向 Boot 2.x 组合，传递的 MyBatis Spring 版本与 Boot 3 生态不完全匹配。`mybatis-plus-spring-boot3-starter:3.5.5` 面向 Boot 3.x，和当前 `mybatis-spring-boot-starter:3.0.3` 一致。

### F-003: 需要保留显式 MyBatis Boot starter

PageHelper `pagehelper-spring-boot-starter:1.4.1` 的自动配置依赖 `org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration`。因此本次只替换 MyBatis-Plus starter，不删除显式 `mybatis-spring-boot-starter:3.0.3`。

### F-004: TK Mapper 是重复数据访问抽象

db-core 已有 `com.fons.cloud.db.mybatisplus` 封装，继续保留 `com.fons.cloud.db.tk` 会让公共数据访问基类存在两套方向。用户已明确允许删除 TK Mapper 依赖和整包，并接受破坏性变更。

### F-005: `CreateTableSql` 的 JPA 注解可删除

`CommonMapper.xml` 使用 `resultMap` 显式映射 `Table` 到 `table`、`Create Table` 到 `createTable`，因此 `CreateTableSql` 上的 `javax.persistence.Column` 注解不是当前 MyBatis XML 映射所必需。

## 决策记录

### D-001: 替换 MyBatis-Plus starter

- **Decision**: 使用 `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5` 替换 `com.baomidou:mybatis-plus-boot-starter:3.5.5`。
- **Reason**: 与 Spring Boot 3.5.8 基线匹配，减少依赖调解风险。
- **Rejected Alternative**: 继续使用旧 starter 并依赖 Maven 版本调解。拒绝原因是长期升级风险高，且与 Boot 3 生态不一致。

### D-002: 保留 MyBatis Boot starter

- **Decision**: 保留 `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3`。
- **Reason**: PageHelper 当前版本依赖 MyBatis Boot 自动配置类。
- **Rejected Alternative**: 删除显式 MyBatis Boot starter，仅依赖 MyBatis-Plus starter。拒绝原因是可能破坏 PageHelper 自动配置。

### D-003: 删除 TK Mapper

- **Decision**: 删除 TK Mapper starter 和 `com.fons.cloud.db.tk` 整包。
- **Reason**: 用户明确授权破坏性删除；项目已有 MyBatis-Plus 公共封装可作为迁移方向。
- **Rejected Alternative**: 保留 TK Mapper 兼容层并标记废弃。拒绝原因是继续引入旧依赖和重复抽象，不符合本次“移除 TK Mapper”的目标。

### D-004: 不升级 PageHelper

- **Decision**: 本次不升级 PageHelper。
- **Reason**: 用户诉求聚焦 MyBatis-Plus starter 替换和 TK Mapper 删除；PageHelper 升级可能引入额外行为变化。
- **Rejected Alternative**: 同步升级 PageHelper 到更新版本。拒绝原因是影响面扩大，不符合 KISS。

## 风险与缓解

- **R-001 调用方编译失败**: 外部模块引用 `com.fons.cloud.db.tk` 会失败。缓解方式：文档明确迁移到 MyBatis-Plus 基类或普通 MyBatis Mapper。
- **R-002 运行时自动配置差异**: starter 替换可能影响 MyBatis-Plus 自动配置顺序。缓解方式：保留现有 `MybatisPlusAutoConfiguration`，并通过 db-core 与 db-shardingsphere 构建验证。
- **R-003 测试覆盖不足**: 本次构建使用 `-DskipTests`。缓解方式：记录残余风险，后续如发现业务调用方需补充对应测试。

