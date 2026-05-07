# Compatibility Contract: TK Mapper 移除后的对外影响

**日期**: 2026-04-30  
**SDD Level**: S2

## 破坏性变更声明

本次变更删除 `fons4cloud-common-db-core` 中的 TK Mapper 依赖和 `com.fons.cloud.db.tk` 整包。用户已明确接受该对外不兼容变更。

## 已删除类型

以下类型不再作为公共 API 提供：

- `com.fons.cloud.db.tk.BaseTkMapper`
- `com.fons.cloud.db.tk.BaseTkService`
- `com.fons.cloud.db.tk.PrimaryLessBaseEntity`
- `com.fons.cloud.db.tk.PrimaryLessTkMapper`
- `com.fons.cloud.db.tk.PrimaryLessTkService`
- `com.fons.cloud.db.tk.model.BaseEntity`
- `com.fons.cloud.db.tk.support.BaseTkServiceImpl`
- `com.fons.cloud.db.tk.support.PrimaryLessTkServiceImpl`

## 调用方迁移规则

### Mapper 迁移

- 原继承 `BaseTkMapper<T>` 的 Mapper，应迁移到 `com.fons.cloud.db.mybatisplus.BasePlusMapper<T>`，前提是实体继承符合 MyBatis-Plus 基类约束。
- 不适合 MyBatis-Plus 的场景，应改为普通 MyBatis `@Mapper` 接口并在 XML 中显式维护 SQL。

### Service 迁移

- 原继承 `BaseTkService<T>` 的 Service，应迁移到 `com.fons.cloud.db.mybatisplus.BasePlusService<T>` 或业务自有 Service。
- 原继承 `BaseTkServiceImpl<M, T>` 的实现，应迁移到 `BasePlusServiceImpl<M, T>` 或业务自有实现。

### 无主键场景

- 原 `PrimaryLessTkMapper` / `PrimaryLessTkService` 场景没有一比一兼容层。
- 调用方必须根据实际 SQL 行为改为普通 MyBatis Mapper 或显式 MyBatis-Plus 自定义方法。

## 保留契约

以下能力仍保留：

- `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3`
- `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.5`
- `com.fons.cloud.db.mybatisplus.BasePlusMapper`
- `com.fons.cloud.db.mybatisplus.BasePlusService`
- `com.fons.cloud.db.mybatisplus.BasePlusServiceImpl`
- db-core 中现有 MyBatis-Plus 自动配置
- `CommonMapper.xml` 对 `CreateTableSql` 的显式 resultMap 映射

## 发布注意事项

- 该变更应作为破坏性变更在发布说明中标记。
- 下游业务模块升级前应先编译验证是否存在 `com.fons.cloud.db.tk` 引用。
- 若短期存在大量下游引用，应在业务迁移分支中逐模块处理，不建议在 db-core 中恢复兼容层。

