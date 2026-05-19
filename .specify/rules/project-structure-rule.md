# 项目结构规则

> 适用范围：fons4cloud Maven 多模块结构、包路径、资源、测试、规则与知识库目录
> 生成依据：用户指定 `$fons4ai-generate-project-rules`、`AGENTS.md`、根与模块 `pom.xml`、源码/资源目录扫描、Spring Boot 自动配置声明
> 规则状态：已有项目提炼

## 项目事实

| 类型 | 已确认事实 | 证据来源 | 状态 |
| --- | --- | --- | --- |
| 根工程 | `fons4cloud` 是 Maven `pom` packaging 父工程，版本使用 `${revision}` | `pom.xml` | 已确认 |
| 一级模块 | `fons4cloud-auth`、`fons4cloud-common`、`fons4cloud-gateway`、`fons4cloud-mq`、`fons4cloud-starter` | `pom.xml` | 已确认 |
| common 子模块 | base、cache、canal、db、elasticsearch、file、limiter、lock、quartz、seata、skywalking、stream、util、web、xxljob 等 | `fons4cloud-common/pom.xml`、文件扫描 | 已确认 |
| auth 子模块 | core、service、service-api、spring-security | `fons4cloud-auth/pom.xml` | 已确认 |
| mq 子模块 | common、api、rocketmq、kafka、rabbitmq | `fons4cloud-mq/pom.xml` | 已确认 |
| starter 子模块 | nacos、dubbo | `fons4cloud-starter/pom.xml` | 已确认 |
| 标准目录 | Java 源码在 `src/main/java`，资源在 `src/main/resources`，测试在 `src/test/java` | 文件扫描 | 已确认 |
| 自动配置 | Spring Boot 3 自动配置声明位于 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | 文件扫描 | 已确认 |
| 规则路径 | 当前技能要求输出 `.specify/rules/`；当前 `AGENTS.md` 仍描述 `rules/` 五件套 | 技能文件、`AGENTS.md` | 待补充约定 |

## 强制规则

- 模块归属规则：新增代码必须放入职责匹配的现有模块；仅当现有模块无法承载稳定职责时，才能新增模块并先完成技术方案。
- 父子 POM 规则：新增模块必须挂载到最近父 `pom.xml`；内部模块版本优先使用 `${revision}` 或父工程依赖管理，禁止私自硬编码内部模块版本。
- common 边界规则：通用基础能力放入 `fons4cloud-common-*`；不得让 common 模块依赖 gateway、具体业务启动模块或仅用于某个业务服务的实现。
- auth 边界规则：认证用户、权限资源、OAuth/Security 相关能力归属 `fons4cloud-auth-*`；网关只接入认证链路，不沉淀认证业务模型。
- gateway 边界规则：网关模块只放路由、过滤器、网关安全接入、限流/熔断接入和启动配置；不得承载下游业务规则。
- mq 边界规则：消息抽象/API 放在 `fons4cloud-mq-api` 或 `fons4cloud-common-stream`；RocketMQ/Kafka/RabbitMQ 细节放入对应实现模块。
- starter 边界规则：starter 模块只放基础设施集成和默认配置，不承载业务规则、实体或持久化逻辑。
- 资源位置规则：新增配置、Mapper XML、SQL 脚本、自动配置导入文件必须放在对应模块的 `src/main/resources` 下；测试资源放在 `src/test/resources`。
- 规则与知识位置规则：本技能生成的项目规则放入 `.specify/rules/`；长期知识默认放入 `.specify/memory/`；DDL 知识默认放入 `.specify/sql/`；SDD 产物默认放入 `specs/`。

## 推荐规则

- 优先复用已有包名：`api`、`core`、`common`、`config`、`autoconfigure`、`support`、`filter`、`handler`、`service`、`utils`、`mapper`、`annotation`。
- 自动配置类优先放在 `config` 或 `autoconfigure` 包，并同步维护 `AutoConfiguration.imports`。
- 共享工具优先放入 `fons4cloud-common-util`；通用响应、异常、基础常量优先放入 `fons4cloud-common-base`。
- Web 过滤器、全局异常、HTTP 上下文相关能力优先放入 `fons4cloud-common-web`，除非是 Gateway Reactive 专属逻辑。
- 涉及 DB、Redis、Seata、Sentinel、MQ、XXL-Job 等基础设施时，优先查找对应 common/mq/starter 模块，不从业务模块临时接入。

## 禁止事项

- 禁止臆造未在 POM 或源码中出现的模块、层级、CI、发布目录或迁移目录。
- 禁止让 API 契约模块依赖 service、web、db 实现层，除非已有设计明确允许并记录兼容影响。
- 禁止将基础设施适配器、Mapper XML、配置类散落到无关业务包。
- 禁止为了单个功能创建长期公共包或跨多个模块复制同一工具。
- 禁止在未确认的情况下恢复、删除或重写当前工作区中已删除的 `.specify/`、`specs/` 历史文件。

## 例外机制

- 新增模块、调整依赖方向或迁移包路径前，必须先形成技术方案，说明职责、替代方案、影响范围、兼容性和验证方式。
- 如果技能输出路径 `.specify/rules/` 与 `AGENTS.md` 中的 `rules/` 描述冲突，本次按用户显式指定技能执行；后续是否同步 `AGENTS.md` 需要用户确认。
- 如果必须临时把文件放在非最终目录，必须在任务或实现说明中记录迁移条件和补齐时间。

## 待确认约定

- 项目规则最终标准目录是否统一为 `.specify/rules/`，还是继续使用 `rules/`。
- 历史 `.specify/memory/constitution.md`、`.specify/memory/java-development-standard.md` 和 `specs/001-*` 是否恢复。
- 是否存在团队级 CI、发布、部署、数据库迁移目录或外部文档仓库。
- API、application、domain、infrastructure 等更细分层是否作为后续新增业务模块的强制结构。

## 验收检查

- [ ] 新增文件位于职责匹配的模块、包和目录。
- [ ] 新增模块已挂载父 POM，并符合依赖方向。
- [ ] 自动配置、Mapper、SQL、YAML 等资源放在对应 `src/main/resources`。
- [ ] 没有让底层 common/api/starter 反向依赖 gateway 或具体实现模块。
- [ ] 新增结构有仓库证据、设计依据或明确标记为默认建议。
- [ ] 没有修改与当前任务无关的目录结构和历史删除状态。
