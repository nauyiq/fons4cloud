# OSS Store Service 需求说明

> Feature: `oss-store-service`
> SDD 等级: `S2`
> 状态: Implementing
> 创建日期: 2026-05-18
> 来源: 用户确认的新 OSS 文件能力实施计划

## 需求概要

| REQ | 说明 | AC |
| --- | --- | --- |
| REQ-001 | 新增独立 `OssStoreService`，承载对象存储上传、下载、存在判断、删除、对象信息和访问 URL 能力，不维护旧 `FileService`。 | AC-001, AC-002 |
| REQ-002 | 新增 `OssUploadRequest`、`OssObjectRequest`、`OssObjectResponse`，表达对象存储专用输入输出。 | AC-003, AC-004, AC-005 |
| REQ-003 | OSS 配置继续使用 `fons4cloud.upload.oss.*` 下的 `CloudSecret`，新增 `enabled` 和 `provider`，默认 provider 为 `ALI_OSS`。 | AC-006, AC-007, AC-008 |
| REQ-004 | 新 OSS 自动配置必须显式启用，未启用时不注册新 Bean，不影响旧 `FileService`。 | AC-009, AC-010 |
| REQ-005 | Ali OSS 与 MinIO 均支持新服务能力，SDK 客户端构造期创建并复用，异常不得泄露密钥。 | AC-011, AC-012, AC-013 |
| REQ-006 | 本次不新增数据库、DDL、Controller、业务模块或旧上传链路改造。 | AC-014 |

## 关键业务规则与约束

- 新 OSS 能力是公共技术能力，不改变业务流程、业务角色或持久化模型。
- 旧 `FileService`、`AbstractFileService`、`AliCloudFileService`、`UploadFileService` 和 `DefaultUploadFileService` 不改签名、不改实现。
- `fons4cloud.upload.oss.enabled=true` 是新能力自动注册门禁；默认不启用。
- `TENCENT_OSS` 保留枚举语义但不接入新 `OssStoreService`，显式配置时启动失败。
- 日志、异常和测试断言不得包含真实 `secretId`、`secretKey`。

## 功能概览

- `OssStoreService#upload(OssUploadRequest)` 上传对象，显式 `objectKey` 优先，缺失时根据日期、场景、可选唯一 ID 和生成文件名创建 object key。
- `download(OssObjectRequest)` 返回对象流和对象元信息。
- `exists/delete/getObjectInfo/getAccessUrl` 提供常用对象级操作。
- Ali OSS 与 MinIO 使用统一路径规则：`objectKey` 优先；`accessUri` 兜底解析并去除 endpoint、bucket 和多余斜杠。
- MinIO 访问 URL 使用 `oss.endpoint + objectKey`，不新增 public endpoint。

## 影响面概览

- Java API: 新增 `OssStoreService` 与专用请求/响应模型。
- 自动配置: 新增独立 OSS 自动配置类，并通过 Spring Boot imports 加载。
- 配置对象: `CloudSecret` 新增 `enabled`、`provider` 字段；`ServerProvider` 新增 `MINIO`。
- 依赖: 文件模块新增 MinIO SDK 依赖。
- 测试: 新增自动配置、核心路径、Ali provider、MinIO provider 测试。
- 数据/DDL: 无影响。

## 工作流概览

```text
下游应用配置 fons4cloud.upload.oss.enabled=true
  -> Spring Boot 加载 OssStoreAutoConfiguration
  -> 读取 CloudSecret(provider/endpoint/bucket/secretId/secretKey)
  -> 根据 provider 创建 AliOssStoreService 或 MinioOssStoreService
  -> 业务代码注入 OssStoreService 执行对象操作
```

## 验收标准

- AC-001: Given 下游代码注入 `OssStoreService`, when 调用 upload/download/exists/delete/getObjectInfo/getAccessUrl, then 可通过统一接口完成对象存储常用操作。
- AC-002: Given 旧代码继续注入 `FileService`, when 本功能引入后, then 旧 `FileService` 相关类和行为不被修改。
- AC-003: Given `OssUploadRequest` 携带 `objectKey`, when 上传, then 使用该 objectKey，不重新生成路径。
- AC-004: Given `OssUploadRequest` 未携带 `objectKey`, when 上传, then 生成 `yyyy-MM-dd/<scene>/<accessUniqueId>/<uuid>.<suffix>` 形式 object key，`accessUniqueId` 为空时省略该路径段。
- AC-005: Given `OssObjectRequest` 未携带 objectKey 但携带 accessUri, when 解析目标对象, then 去除 endpoint、bucket 和多余斜杠得到 objectKey。
- AC-006: Given 未配置 `fons4cloud.upload.oss.enabled=true`, when 应用启动, then 不注册 `OssStoreService`。
- AC-007: Given `enabled=true` 且未配置 provider, when 应用启动, then 默认注册 Ali OSS 实现。
- AC-008: Given `enabled=true` 且 provider 为 `MINIO`, when 应用启动, then 注册 MinIO 实现。
- AC-009: Given `enabled=true` 且缺少 endpoint、bucket、secretId 或 secretKey, when 应用启动, then 启动失败且错误信息不泄露密钥。
- AC-010: Given 已存在用户自定义 `OssStoreService`, when 自动配置执行, then 不覆盖用户 Bean。
- AC-011: Given 上传请求携带 metadata, when Ali OSS 或 MinIO 上传, then metadata 传递到对应 SDK 请求。
- AC-012: Given Ali OSS 或 MinIO provider 已创建, when 多次调用对象操作, then 复用构造期创建的 SDK 客户端。
- AC-013: Given provider SDK 抛出异常, when 转换为文件模块异常, then 日志和异常消息不输出 secretId/secretKey。
- AC-014: Given 本功能实施, when 检查数据模型和业务入口, then 不新增数据库、DDL、Controller 或业务模块改动。

## 非功能要求

- 兼容性: 默认不启用新 OSS Bean，不影响旧文件服务。
- 安全性: 密钥类配置不得出现在异常消息、响应或测试输出中。
- 可维护性: 新能力放入 `fons4cloud-common-file` 既有模块，不新增 Maven 子模块。
- 性能: 上传和下载保持流式处理，不读取完整文件到内存。

## 数据与领域对象

- `OssStoreService`: 对象存储服务接口，无持久化。
- `OssUploadRequest`: 上传请求模型，无持久化。
- `OssObjectRequest`: 对象定位请求模型，无持久化。
- `OssObjectResponse`: 对象信息响应模型，无持久化。
- DDL sync expected: no。

## 风险概览

- 新接口属于公共 API，需通过 default-off 自动配置降低旧应用风险。
- MinIO SDK 新依赖可能引入传递依赖冲突，需模块测试验证。
- `.specify/memory/technical-architecture.md` 现有旧对象存储记录与当前源码不一致，实施后需同步为新事实。
