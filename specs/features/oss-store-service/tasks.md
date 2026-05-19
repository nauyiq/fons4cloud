# OSS Store Service 实施任务

> Feature: `oss-store-service`
> SDD 等级: `S2`
> Source Spec: `specs/features/oss-store-service/spec.md`
> Source Plan: `specs/features/oss-store-service/plan.md`
> 状态: Completed

## 执行策略

- 先写自动配置与核心行为测试形成 RED。
- 再新增 API、模型、抽象基类和 provider 实现。
- 最后跑模块测试、更新报告和技术架构知识。

## 实现确认门禁

- Status: approved by latest user message.
- Latest user message explicitly requested implementation of this plan.
- If no task IDs are specified, execute all unfinished tasks.

## Tasks

- [x] T001 编写新 OSS 自动配置和核心契约测试
  - AC: AC-001, AC-003, AC-004, AC-005, AC-006, AC-007, AC-008, AC-009, AC-010, AC-011
  - Files: `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/config/OssStoreAutoConfigurationTest.java`; `fons4cloud-common/fons4cloud-common-file/src/test/java/com/fons/cloud/file/core/oss/AbstractOssStoreServiceTest.java`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=OssStoreAutoConfigurationTest,AbstractOssStoreServiceTest test`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: RED 信号能证明新接口、模型和自动配置尚未实现。

- [x] T002 新增 OSS API、模型和配置开关
  - AC: AC-001, AC-003, AC-004, AC-005, AC-006, AC-007, AC-008, AC-009, AC-010
  - Files: `OssStoreService`; `OssUploadRequest`; `OssObjectRequest`; `OssObjectResponse`; `CloudSecret`; `ServerProvider`; `pom.xml`; `AutoConfiguration.imports`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=OssStoreAutoConfigurationTest,AbstractOssStoreServiceTest test`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: 新 API 和配置开关完成，旧 `FileService` 文件未修改。

- [x] T003 实现共享抽象基类和自动配置
  - AC: AC-003, AC-004, AC-005, AC-006, AC-007, AC-008, AC-009, AC-010, AC-013
  - Files: `AbstractOssStoreService`; `OssStoreAutoConfiguration`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=OssStoreAutoConfigurationTest,AbstractOssStoreServiceTest test`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: 配置、路径、URL、异常和自动配置行为测试通过。

- [x] T004 实现 Ali OSS 新 provider
  - AC: AC-001, AC-011, AC-012, AC-013
  - Files: `AliOssStoreService`; `AliOssStoreServiceTest`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=AliOssStoreServiceTest test`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: Ali provider 六个对象能力和客户端销毁测试通过。

- [x] T005 实现 MinIO 新 provider
  - AC: AC-001, AC-008, AC-011, AC-012, AC-013
  - Files: `MinioOssStoreService`; `MinioOssStoreServiceTest`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file -Dtest=MinioOssStoreServiceTest test`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: MinIO provider 六个对象能力和客户端复用测试通过。

- [x] T006 模块回归、报告和知识同步
  - AC: AC-001, AC-002, AC-006, AC-009, AC-014
  - Files: `fons4cloud-common/fons4cloud-common-file/**`; `specs/features/oss-store-service/reports/implementation-report.md`; `.specify/memory/technical-architecture.md`
  - Verification: `mvn -pl fons4cloud-common/fons4cloud-common-file test`; `validate_sdd_artifacts.py --feature-dir specs/features/oss-store-service`
  - Quality: confirm readability, DDD-lite/domain-modeling check, method size, naming, duplicate-code check, utility reuse, and dependency gate
  - Done: 模块回归和 SDD 校验通过，知识同步完成。

## S2 质量门禁

- [x] 兼容门禁：确认旧 `FileService`、`AbstractFileService`、`AliCloudFileService` 未修改。
- [x] 安全门禁：确认配置缺失和 SDK 异常不泄露 secret。
- [x] 回滚门禁：确认移除新自动配置 imports 即可关闭新能力。
