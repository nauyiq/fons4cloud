# 数据模型与 DDL 规则

> 适用范围：fons4cloud 数据模型、Mapper XML、SQL 脚本、事务一致性与 `.specify/sql` DDL 知识
> 生成依据：用户指定 `$fons4ai-generate-project-rules`、`AGENTS.md`、`sys_auth.sql`、`seata.sql`、`xxljob.sql`、Mapper XML
> 规则状态：已有项目提炼

## 项目事实

| 类型 | 已确认事实 | 证据来源 | 状态 |
| --- | --- | --- | --- |
| 认证库 | `sys_auth.sql` 定义 `sys_auth.account`、`sys_auth.oauth_client`、`sys_auth.tcc_fence_log`、`sys_auth.undo_log` | `fons4cloud-auth/fons4cloud-auth-service/src/main/resources/sys_auth.sql` | 已确认 |
| Seata 表 | `seata.sql` 定义 `undo_log`、`tcc_fence_log`、`global_table`、`branch_table`、`lock_table`、`distributed_lock` | `fons4cloud-common/fons4cloud-common-seata/db/seata.sql` | 已确认 |
| XXL-Job 库 | `xxljob.sql` 创建 `xxl_job` 库和 `xxl_job_*` 表 | `fons4cloud-common/fons4cloud-common-xxljob/src/main/resources/xxljob.sql` | 已确认 |
| Mapper XML | `AccountMapper.xml` 映射敏感字段 `id_card`、`real_name` 使用 `AesEncryptTypeHandler` | `AccountMapper.xml` | 已确认 |
| 动态 SQL | `CommonMapper.xml` 存在 `SHOW CREATE TABLE ${tableName}` 和 `executeSql ${sql}` | `CommonMapper.xml` | 已确认 |
| DDL 知识路径 | `AGENTS.md` 当前要求 `.specify/sql/<database_or_service>/<business_model>.sql`，不同数据库/服务必须拆分 | `AGENTS.md` | 已确认 |
| 迁移工具 | 未发现 Flyway、Liquibase 或统一迁移目录 | 仓库扫描 | 待补充约定 |

## 强制规则

- 模型变更规则：新增、删除、重命名或修改持久化模型、表、字段、索引、约束、默认值、关系、状态枚举时，必须同步设计、任务、测试和 DDL 知识。
- DDL 知识路径规则：DDL 知识文件必须使用 `.specify/sql/<database_or_service>/<business_model>.sql`；其中 `<database_or_service>` 表达数据库、服务库或明确物理数据源。
- 分组规则：同一数据库/服务内强业务耦合的多张表可以放入同一个业务模型 SQL 文件；不同数据库、服务库或物理数据源必须拆分，禁止跨库合并。
- 文件头规则：每个 SQL 知识文件必须标明数据库/服务、业务模型、包含表、来源证据、状态和更新时间。
- 迁移边界规则：`.specify/sql/**/*.sql` 是知识库文件，不替代项目自身 SQL 脚本、迁移脚本或生产变更单。
- 安全字段规则：密码、token、client_secret、id_card、real_name、secretKey 等敏感字段必须有加密、脱敏或访问边界说明。
- Mapper 规则：新增 Mapper XML SQL 时必须显式评估 `SELECT *`、`${}` 动态拼接、`AND/OR` 优先级、批量更新/删除条件和 SQL 注入风险。
- 事务规则：涉及多表写入、DB + 缓存、DB + MQ、Seata、分布式锁或外部副作用时，必须明确事务边界、幂等策略、回滚/补偿策略和失败处理。

## 推荐规则

- 优先从 SQL 脚本、实体、Mapper XML、配置和已有 DDL 知识交叉确认表结构，不从单一命名臆测。
- 认证域 DDL 优先按 `sys_auth` 数据库/服务归档，账号与 OAuth 客户端可归为认证业务模型，Seata 认证库表需单独说明事务用途。
- Seata 服务端/客户端表优先按 Seata 数据源归档，避免与业务库表混写。
- XXL-Job 表优先按 `xxl_job` 数据库和调度业务模型归档。
- 表字段应保留来源脚本中的注释、主键、唯一索引、普通索引、默认值和字符集信息。
- 新增状态字段、软删除字段、乐观锁字段、审计时间字段时，应补充生命周期和查询过滤规则。

## 禁止事项

- 禁止在证据不足时臆造 DDL、字段类型、索引、默认值或约束。
- 禁止把不同数据库、服务库或物理数据源的表合并到一个 DDL 知识文件。
- 禁止只修改代码、Mapper 或资源 SQL，而遗漏 `.specify/sql/` 与数据架构知识同步。
- 禁止把 `.specify/sql/**/*.sql` 当作可直接执行的生产迁移脚本。
- 禁止新增裸 `${}` 拼接外部输入；确需动态表名、排序字段或 SQL 执行时，必须有白名单或受控来源。
- 禁止在未确认条件范围的情况下执行无条件更新、删除或批量操作。

## 例外机制

- 如果 DDL 同步必须延期，必须在 SDD 计划或任务中记录 owner、延期原因、风险、补齐时机和临时验证方式。
- 如果数据库/服务归属无法确认，必须标记为 `待确认`，不得放入默认公共 SQL 文件。
- 如果必须使用 `${}` 动态 SQL，必须说明输入来源、白名单约束、调用边界和测试覆盖。
- 如果知识库文件与现有资源 SQL 不一致，必须以仓库可执行脚本或用户确认的数据库事实为准，并记录差异。

## 待确认约定

- 是否存在正式数据库迁移工具、生产变更单或版本化 SQL 目录。
- `.specify/sql/` 是否需要按当前 `AGENTS.md` 调整为二级目录结构，而不是扁平 SQL 文件。
- 认证域账号、客户端、权限资源的完整状态枚举和生命周期。
- Seata 表脚本中 `db/seata.sql` 与 `src/main/resources/seata-server.sql` 的实际部署边界。
- Mapper XML 中历史 `SELECT *` 与 `${}` 动态 SQL 是否纳入专项治理。

## 验收检查

- [ ] 数据模型变更已同步 SDD 任务、测试、资源 SQL 和 `.specify/sql/` 知识文件。
- [ ] DDL 知识文件路径符合 `.specify/sql/<database_or_service>/<business_model>.sql` 分组原则，或已记录待调整原因。
- [ ] 跨数据库、跨服务库、跨物理数据源的表没有合并。
- [ ] SQL 文件头包含数据库/服务、业务模型、包含表、来源证据、状态和更新时间。
- [ ] Mapper XML 新增或修改 SQL 已评估 `SELECT *`、`${}`、`AND/OR`、批量更新/删除风险。
- [ ] 敏感字段、事务边界、幂等和回滚/补偿策略已说明。
