# OSS Store Service 实施报告

> 功能标识：`oss-store-service`
> 任务范围：`T001`-`T006`
> 完成日期：2026-05-18

## 实施摘要

- 已完成任务：T001, T002, T003, T004, T005, T006
- SDD 等级：S2
- 实施结果：新增独立 `OssStoreService` 对象存储能力，支持 Ali OSS 和 MinIO；新能力仅在 `fons4cloud.upload.oss.enabled=true` 时注册，不改动旧 `FileService`、`AbstractFileService`、`AliCloudFileService` 和旧上传链路。

## 变更文件

- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/api/OssStoreService.java`：新增 OSS 对象存储接口。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/common/request/OssUploadRequest.java`：新增上传请求模型。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/common/request/OssObjectRequest.java`：新增对象定位请求模型。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/common/response/OssObjectResponse.java`：新增对象响应模型。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/core/oss/AbstractOssStoreService.java`：新增共享 OSS 支撑抽象。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/core/oss/AliOssStoreService.java`：新增 Ali OSS provider。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/core/oss/MinioOssStoreService.java`：新增 MinIO provider。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/config/OssStoreAutoConfiguration.java`：新增 OSS 自动配置。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/common/CloudSecret.java`：新增 `enabled` 与 `provider` 配置字段。
- `fons4cloud-common/fons4cloud-common-file/src/main/java/com/fons/cloud/file/common/constants/ServerProvider.java`：新增 `MINIO`。
- `fons4cloud-common/fons4cloud-common-file/pom.xml`：新增 MinIO SDK 依赖。
- `fons4cloud-common/fons4cloud-common-file/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：新增新自动配置入口。
- `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/config/OssStoreAutoConfigurationTest.java`：新增自动配置测试。
- `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/core/oss/AbstractOssStoreServiceTest.java`：新增共享核心规则测试。
- `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/core/oss/AliOssStoreServiceTest.java`：新增 Ali provider 测试。
- `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/core/oss/MinioOssStoreServiceTest.java`：新增 MinIO provider 测试。
- `.specify/memory/technical-architecture.md`：同步文件模块新旧 OSS 能力边界。

## TDD 记录

| 任务 | RED | GREEN | REFACTOR |
| --- | --- | --- | --- |
| T001 | 新测试编译失败，缺少 `OssStoreService`、OSS 模型、provider 和 MinIO 依赖 | 新增 API、模型、自动配置和 provider 后测试进入行为验证 | 调整异常 code 与 MinIO metadata 断言 |
| T002 | API/模型引用不存在 | 新增 `OssStoreService`、`OssUploadRequest`、`OssObjectRequest`、`OssObjectResponse` | 保持请求模型轻量，避免复用旧 `FileService` 语义 |
| T003 | 自动配置类不存在，默认开关行为无法验证 | 新增 `OssStoreAutoConfiguration` 和共享抽象基类 | 统一配置校验、key 生成、URL 解析和敏感信息保护 |
| T004 | Ali provider 不存在 | 新增 `AliOssStoreService`，构造期创建并销毁 OSS 客户端 | provider 只负责 SDK 调用，共享规则留在抽象基类 |
| T005 | MinIO provider 不存在且缺依赖 | 新增 MinIO SDK 依赖和 `MinioOssStoreService` | 客户端构造期创建，测试覆盖 metadata 与客户端复用 |
| T006 | 未完成回归、报告和知识同步 | 文件模块测试、SDD 校验通过 | 关闭 S2 兼容、安全和回滚门禁 |

## 验证结果

- 命令：`JAVA_HOME=C:\hongqy\C\Java\jdk21; mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=OssStoreAutoConfigurationTest,AbstractOssStoreServiceTest,AliOssStoreServiceTest,MinioOssStoreServiceTest test`
- 结果：通过，22 tests, 0 failures, 0 errors。
- 命令：`JAVA_HOME=C:\hongqy\C\Java\jdk21; mvn -pl fons4cloud-common/fons4cloud-common-file test`
- 结果：通过，22 tests, 0 failures, 0 errors。
- 命令：`validate_sdd_artifacts.py --feature-dir specs/features/oss-store-service`
- 结果：通过。

## AC 覆盖

| AC | 证据 |
| --- | --- |
| AC-001 | `OssStoreService` 暴露 upload/download/exists/delete/getObjectInfo/getAccessUrl。 |
| AC-002 | `git diff --name-only` 确认旧 `FileService`、`AbstractFileService`、`AliCloudFileService`、旧上传链路文件未修改。 |
| AC-003 | `AbstractOssStoreServiceTest#shouldUseExplicitObjectKeyWhenUpload`。 |
| AC-004 | `shouldGenerateObjectKeyWhenObjectKeyMissing` 与 `shouldOmitAccessUniqueIdWhenGenerateObjectKey`。 |
| AC-005 | `shouldResolveAccessUriToObjectKey` 与 `shouldPreferObjectKeyOverAccessUri`。 |
| AC-006 | `OssStoreAutoConfigurationTest#shouldNotRegisterOssStoreServiceWhenDisabled`。 |
| AC-007 | `shouldRegisterAliOssStoreServiceWhenEnabledWithoutProvider`。 |
| AC-008 | `shouldRegisterMinioOssStoreServiceWhenProviderIsMinio`。 |
| AC-009 | `shouldFailWithoutLeakingSecretWhenRequiredConfigMissing`。 |
| AC-010 | `shouldNotOverrideCustomOssStoreService`。 |
| AC-011 | Ali/MinIO upload metadata 测试。 |
| AC-012 | Ali 构造期客户端销毁测试、MinIO mock 客户端复用路径测试。 |
| AC-013 | Ali/MinIO SDK 异常不泄露凭证测试。 |
| AC-014 | 未新增数据库、DDL、Controller 或业务模块。 |

## 代码质量复盘

- 可读性检查：是，公共规则集中在 `AbstractOssStoreService`。
- 方法长度与职责检查：是，provider 方法只封装 SDK 调用。
- 命名表达力检查：是，新增类型均为 OSS 语义。
- 领域建模复盘：接受轻量模型，原因是本能力为公共基础设施 API，无持久化领域对象。
- 应用层编排检查：不适用，本次没有业务应用服务。
- 基础设施依赖边界检查：是，Ali/MinIO SDK 只在 file 模块 provider 中使用。
- 重复逻辑检查：无重复，配置校验、key 生成、URL 解析复用抽象基类。
- 工具复用：JDK、Hutool、Apache Commons、项目已有 `FileException`。
- 新增依赖：是，MinIO SDK，已由 SDD 方案确认。
- 异常与日志风格检查：是，SDK 异常转为通用 `FileException`，不拼接凭证。
- 测试可读性检查：是，按自动配置、共享规则、Ali、MinIO 分组。

## 问题与后续事项

- Maven 输出存在既有 `fons4cloud-auth-service-api` 版本表达式 warning，与本次文件模块改动无关。
- Mockito 在 Java 21 下提示未来需配置 agent，当前不影响测试结果。

## S2 门禁关闭情况

- Checklist 关闭：是。
- 回滚方案验证：是，移除 `OssStoreAutoConfiguration` import 即可关闭新能力自动注册；默认 `enabled=false` 不影响旧链路。
- 兼容性风险关闭：是，旧 `FileService` 和旧上传链路未修改。
- 安全/权限风险关闭：是，配置缺失和 SDK 异常消息不泄露凭证。
- 其他风险控制任务关闭：是，MinIO 依赖已通过文件模块测试验证。

## 知识同步需求

- 业务架构：否。
- 技术架构：是，已更新 `.specify/memory/technical-architecture.md`。
- 数据架构：否。
- 其他真理源：否。
- SQL DDL 文件：否。
- 原因：本次仅新增文件模块 OSS 基础设施能力，无数据库、DDL、Controller 或业务入口改动。
- 建议后续：如团队需要长期沉淀到知识库，可继续执行 `fons4ai-knowledge-summary`。
