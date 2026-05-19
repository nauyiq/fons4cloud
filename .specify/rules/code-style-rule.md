# 代码风格规则

> 适用范围：fons4cloud Java 源码、配置类、测试代码与公共 API
> 生成依据：用户指定 `$fons4ai-generate-project-rules`、`AGENTS.md`、根与模块 `pom.xml`、代表性源码、现有测试、Mapper XML
> 规则状态：已有项目提炼

## 项目事实

| 类型 | 已确认事实 | 证据来源 | 状态 |
| --- | --- | --- | --- |
| 语言/运行时 | Java 21，源码和目标编译版本均为 21，UTF-8 编码 | `pom.xml` | 已确认 |
| 核心框架 | Spring Boot 3.5.8、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0、Dubbo 3.3.0 | `pom.xml` | 已确认 |
| 包名 | 主包以 `com.fons.cloud` 为主，MQ common 存在 `com.fons.mq` 包系 | `rg --files -g '*.java'` | 已确认 |
| 注入方式 | 已有 `@RequiredArgsConstructor` + `final` 字段，也存在显式 `@Bean` 方法和手写 Logger | `DistributeLockAspect`、`WebMvcGlobalExceptionHandler` | 已确认 |
| 响应与异常 | Web 响应使用 `R`、`ResultCode`，业务异常使用 `BizException` 或模块异常 | `WebMvcGlobalExceptionHandler`、`ResultCode` | 已确认 |
| 日志 | 使用 SLF4J / `@Slf4j`，日志参数化占位符已存在 | `SecurityAuthenticationFilter`、`DistributeLockAspect` | 已确认 |
| 注释 | 关键类、配置字段、复杂逻辑已有中文注释和 Javadoc 风格说明 | `GatewayMain`、`DistributeLockAspect`、SQL 字段注释 | 已确认 |
| 格式化工具 | 未发现 Checkstyle、Spotless、EditorConfig 等统一格式化规则 | 仓库文件扫描 | 待补充约定 |

## 强制规则

- Java 版本规则：新增或修改 Java 代码时，必须保持 Java 21 兼容；除非根 POM 先调整版本并完成兼容性验证。
- 包名规则：新增包必须沿用所在模块包系；通用框架代码优先放在 `com.fons.cloud` 下，禁止为局部需求新增无来源根包。
- 命名规则：类名使用大驼峰，方法/字段/局部变量使用小驼峰，常量使用全大写下划线；Request、Response、Info、Properties、Configuration、AutoConfiguration、Filter、Aspect、Service、Template、Factory、Handler 后缀必须表达真实职责。
- 注入规则：新增 Spring Bean、组件、切面、配置类时，优先使用构造注入；使用 Lombok 时优先 `@RequiredArgsConstructor` + `final` 字段。仅当框架要求或已有局部风格明确时，允许使用显式 `@Bean` 方法。
- 响应规则：HTTP/RPC 对外可观察结果必须优先使用项目既有响应模型，例如 `R<T>`、`Result`、`ResultCode`；不得在同一接口族混用裸对象、`Map` 和自定义临时响应结构。
- 异常规则：业务失败必须优先使用 `BizException`、模块专用异常或实现 `Result` 的错误码；禁止用裸 `RuntimeException` 表达可预期业务失败。
- 日志规则：新增异常路径、外部交互、锁、认证、限流、MQ、事务等关键路径日志时，必须使用参数化日志并带可定位上下文；异常日志必须传入异常对象。
- 敏感信息规则：日志、异常消息、测试数据和文档示例禁止输出 token、Authorization、clientSecret、password、secretId、secretKey、身份证、手机号全量和生产数据。
- 注释规则：新增关键逻辑、复杂分支、并发/锁、鉴权、限流、事务、数据转换、配置字段含义时，必须写简洁中文注释解释意图、边界或字段语义。

## 推荐规则

- 优先复用 `fons4cloud-common-*` 中已有工具、响应、异常、配置、缓存、锁、限流、Web、DB、文件和 Stream 能力。
- 优先保持方法单一职责；当一个方法同时处理校验、转换、外部调用和结果组装时，应抽取私有方法或领域方法。
- 优先使用已有校验方式，例如 Bean Validation、`AssertUtil`、模块已有校验链；不要为单个场景新增校验框架。
- DTO、Request、Response 可使用 Lombok 简化样板；公共实体、含敏感字段或参与集合比较的类型应谨慎使用 `@Data`。
- 复杂 SQL、动态 SQL、加密字段映射和状态字段应通过注释说明业务含义和安全边界。

## 禁止事项

- 禁止为一次局部改动引入新框架、新注解体系、新日志库或新 JSON 工具，除非 SDD 技术方案已批准。
- 禁止把批量格式化、重命名、导入重排、无关重构混入功能变更。
- 禁止在代码、测试、文档中保留临时调试输出、无意义 TODO、废弃大段注释和未验证猜测。
- 禁止复制已有工具类、响应模型、异常模型、缓存/锁/限流/MQ 封装。
- 禁止直接吞异常后返回默认成功结果；无法处理时必须记录上下文并返回明确失败或抛出明确异常。

## 例外机制

- 如果必须偏离所在模块风格，必须在 SDD 计划、实现说明或代码注释中记录触发条件、偏离原因、影响范围和验证方式。
- 如果同一仓库存在多种旧风格，优先遵循当前修改模块内的主流风格；全局统一问题列入 `待确认约定`，不得在功能变更中顺手治理。
- 如果敏感信息需要参与排查，只能输出脱敏值、哈希摘要或内部追踪 ID，并在验证说明中标记原因。

## 待确认约定

- 是否恢复并强制使用历史 `.specify/memory/java-development-standard.md`。
- 是否引入统一格式化器、静态检查或 import 顺序校验。
- 是否统一手写 Logger 与 `@Slf4j` 的使用边界。
- 是否统一接口层 DTO 命名，例如 `XxxInfo`、`XxxResponse`、`XxxRequest` 的适用场景。

## 验收检查

- [ ] 新增代码使用 Java 21 可编译语法和项目现有依赖。
- [ ] 命名、包路径、注入、异常、响应、日志与所在模块主流风格一致。
- [ ] 没有引入未批准的新框架、库或全局风格。
- [ ] 关键逻辑、配置字段和非显然边界已有简洁中文注释。
- [ ] 敏感信息没有出现在日志、异常、测试数据或文档示例中。
- [ ] 默认建议和待确认项没有被写成已确认事实。
