# fons4cloud Java 开发规范

## AI 执行摘要

Codex、Cursor、Claude 等 AI 助手在本项目进行 Java 设计、编码、重构或评审前必须执行：

1. 先读取相关 Java 文件、Mapper XML、POM、配置、调用方、被调用方和测试。
2. 优先复用已有公共能力，不新增重复工具类、组件、starter 或基础设施。
3. Spring Bean 使用构造注入，优先 `@RequiredArgsConstructor` + `final` 字段。
4. 对外接口统一返回 `R<T>` 或项目既有响应模型。
5. 业务异常统一使用 `BizException` + `ResultCode` 或业务域 `XxxResultCode`。
6. Entity 不直接暴露给外部接口，使用 Converter 转换为 `XxxInfo` / `XxxResponse`。
7. Mapper XML 禁止 `SELECT *`，`AND` / `OR` 混用必须加括号。
8. 日志使用参数化写法，禁止输出密码、token、身份证、手机号全量、clientSecret 等敏感信息。
9. 修改或删除已有代码前必须获得用户确认，除非当前请求已明确要求。
10. 实现完成后必须说明验证方式、未验证原因和残余风险。

## 一、基本原则

1. 简洁优先：优先使用项目已有公共能力，不重复封装 `JsonUtil`、`R`、`BizException`、`ResultCode`、`CommonEntity`、`BaseEntity`、缓存、限流、Dubbo、MyBatis-Plus 等基础设施。
2. 分层清晰：代码必须按 API、facade、application、domain、infrastructure、common/starter 等职责分层，禁止业务逻辑散落在 Controller、Mapper、Converter、Config 中。
3. 行为明确：方法返回值、异常、空值语义必须清楚。不得用 `null` 表达“成功但无数据”，接口层统一返回 `R.ok()` 或 `R.failed(...)`。
4. 先读后改：修改已有逻辑前必须阅读调用方、被调用方、实体、Mapper、异常码和测试/配置上下文。

## 二、工程与模块

1. Maven 版本统一由根 POM 管理，业务模块版本使用 `${revision}`，禁止子模块私自硬编码内部模块版本。
2. 新增模块命名遵循现有格式：`fons4cloud-{领域}` 或 `fons4cloud-common-{能力}`。
3. API 契约放在 `*-service-api`；实现放在 `*-service`；公共能力放在 `fons4cloud-common-*`；自动装配放在 `autoconfigure` 或 `config`。
4. API 模块不得依赖 service、web、db 实现层，避免实现细节污染契约。

## 三、包与命名

1. 根包使用 `com.fons.cloud`。
2. 请求对象使用 `XxxRequest`，查询参数使用 `XxxQueryParams`，响应对象使用 `XxxInfo` 或 `XxxResponse`。
3. 应用服务使用 `XxxApplicationService`，领域服务使用 `XxxDomainService`，远程门面使用 `XxxFacadeService`，实现类使用 `XxxImpl`。
4. 转换器使用 `XxxConverter`，常量使用 `XxxConstants`，错误码使用 `XxxResultCode`。
5. 方法名使用动宾结构，查询类方法用 `find/query/list/get` 区分语义。可返回空值的查询优先使用 `find` 或 `query`。

## 四、格式与代码布局

1. 使用 UTF-8、4 空格缩进，禁止 Tab。
2. import 顺序为 JDK、第三方、项目包；禁止不必要的通配符 import，尤其避免 `lombok.*`。
3. 类成员顺序建议为常量、依赖字段、普通字段、初始化方法、公开方法、私有方法。
4. 单方法建议不超过 80 行。超过时优先抽取私有方法或领域方法。
5. 条件分支优先早返回，减少深层嵌套。
6. 新增代码不得保留大段空行、调试日志、废弃代码块和无意义注释。

## 五、Lombok 使用

1. Spring Bean 优先使用 `@RequiredArgsConstructor` + `final` 字段构造注入。
2. DTO、Request、Response 可使用 `@Getter`、`@Setter`、`@ToString`、`@NoArgsConstructor`、`@AllArgsConstructor`。
3. 不推荐在实体类使用 `@Data`。实体类建议显式使用 `@Getter`、`@Setter`、`@ToString`，避免 `equals/hashCode` 引入继承、懒加载或敏感字段问题。
4. `@SneakyThrows` 仅允许在框架适配、序列化兼容等边界场景使用，业务方法禁止用它隐藏异常语义。
5. `@Builder` 用于复杂入参构造，简单 DTO 不强制使用。

## 六、接口与响应

1. HTTP/RPC 对外接口统一返回 `R<T>`，不得直接返回裸对象、`Map` 或 `null`。
2. 成功响应使用 `R.ok(data)` 或 `R.ok()`；失败响应使用 `R.failed(Result)` 或 `R.failed(code, message)`。
3. 请求参数必须使用 `jakarta.validation` 注解表达基础校验，例如 `@NotNull`、`@NotBlank`。
4. Controller 只做协议适配、参数接收、响应包装，业务编排放到 application/facade。
5. Dubbo 服务使用 `@DubboService(version = DubboConstants.DEFAULT_DUBBO_SERVICE_VERSION)`；引用使用 `@DubboReference` 并显式版本。

## 七、异常与错误码

1. 业务异常统一使用 `BizException`，错误码实现 `Result` 接口。
2. 通用错误码放 `ResultCode`；业务域错误码放各自 `XxxResultCode`。
3. 错误码按参数异常、数据异常、认证异常、业务异常、系统异常等类别分段。
4. 禁止抛出裸 `RuntimeException` 表达业务失败。
5. 不允许吞异常后返回默认成功值。无法处理时记录关键上下文并抛出或返回明确失败结果。

## 八、日志规范

1. 使用 `@Slf4j`，禁止 `System.out` 和 `System.err`。
2. 日志必须参数化：`log.error("xxx id:{}", id, e)`，禁止字符串拼接。
3. error 日志必须包含异常对象或足够定位的上下文。
4. 禁止打印密码、token、身份证、手机号全量、客户端密钥等敏感数据。
5. 业务成功日志只记录关键节点，避免高频接口打印 info。

## 九、数据库与 MyBatis

1. 实体优先继承 `CommonEntity` / `BaseEntity`，复用 `id`、`deleted`、`version`、`created`、`updated`。
2. MyBatis-Plus 简单查询优先使用 `Wrappers` / `QueryWrapper`；复杂 SQL 使用 Mapper XML。
3. Mapper XML 禁止 `SELECT *`，必须显式列名，避免字段变更影响接口。
4. SQL 中 `AND` 与 `OR` 混用必须加括号。
5. SQL 关键字统一大写，表字段统一小写下划线。
6. 所有外部入参必须使用 `#{}` 绑定，禁止 `${}` 拼接动态值，除非白名单控制排序字段、表名等元数据。
7. 更新、删除必须带明确条件；批量操作必须评估数据量和事务边界。
8. 乐观锁字段 `version`、逻辑删除字段 `deleted` 不得手动绕过。

## 十、事务与一致性

1. 涉及多次数据库写入、数据库写入后缓存变更、远程调用参与状态变更时，必须显式评估事务边界。
2. 本地数据库事务使用 Spring `@Transactional`，只标注在 public service 方法上。
3. 事务方法内避免执行不可回滚的外部副作用；必须执行时要有幂等或补偿策略。
4. 缓存删除/刷新必须与数据库写入结果绑定，禁止先删缓存后写库失败导致不一致。

## 十一、转换与对象模型

1. Entity 不直接暴露给外部接口，统一转换为 `XxxInfo` / `XxxResponse`。
2. 对象转换优先使用 MapStruct `XxxConverter`，避免手写重复字段赋值。
3. Request 不应直接作为 Entity 入库；必须通过应用服务或领域方法表达业务含义。
4. 领域实体可保留有业务含义的方法，例如 `register`、`auth`、`isDisabled`，避免所有逻辑都堆在 service。

## 十二、安全与敏感信息

1. 密码必须使用 `PasswordEncoder`，禁止明文存储和明文日志。
2. 身份证、真实姓名等敏感字段使用已有 `AesEncryptTypeHandler` 或等价机制。
3. token、clientSecret、accessSecret 不得出现在普通响应、日志、异常信息中。
4. 鉴权、限流、黑白名单、安全检查优先复用现有 auth/gateway/limiter 能力。
5. 对外接口必须校验租户、授权类型、scope、账号状态。

## 十三、缓存与 Redis

1. 缓存 key 必须集中定义，禁止散落硬编码。
2. JetCache 注解必须显式 `name`、`key`、`expire`，缓存空值需确认业务可接受。
3. Redis 操作优先复用已有 `RedisManager`、`SmartRedisManager`、`RandomCodeService`。
4. 分布式锁、验证码、幂等、限流等场景不得临时自造 Redis 协议。

## 十四、注释与文档

1. 公共接口、复杂业务方法、Mapper 方法必须写 Javadoc，说明参数和返回语义。
2. 注释解释“为什么”，不重复描述代码“做了什么”。
3. TODO 必须带责任人或后续处理条件，禁止长期遗留模糊 TODO。
4. 作者、日期格式沿用项目现状即可，但新增代码不强制为所有私有方法补 Javadoc。

## 十五、测试与质量

1. 新增业务规则必须配套单元测试或集成测试，至少覆盖成功、失败、边界输入。
2. Mapper XML 的复杂 SQL 必须覆盖条件组合测试，尤其是 `AND/OR` 优先级。
3. 安全相关、状态流转、缓存一致性、幂等逻辑必须有回归测试。
4. 提交前至少运行受影响模块测试；无法运行时必须说明原因和风险范围。
