# OSS Store Service 技术方案

> Feature: `oss-store-service`
> SDD 等级: `S2`
> Source Spec: `specs/features/oss-store-service/spec.md`
> 状态: Implementing

## 仓库事实

- 文件模块位于 `fons4cloud-common/fons4cloud-common-file`。
- Spring Boot 自动配置入口为 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 当前旧 `FileService` 默认由 `UploadFileAutoConfiguration#fileService()` 注册 `AliCloudFileService`。
- `UploadFileProperties` 已使用 `fons4cloud.upload` 作为配置根，`oss` 字段类型为 `CloudSecret`。
- `BaseFileUploadRequest` 已有 `metadata`，可复用到新 OSS 上传请求语义。
- 当前 `pom.xml` 已有 Ali OSS 和 Tencent COS 依赖，尚无 MinIO 依赖。

## 设计概要

新增独立 `OssStoreService`，不继承、不委托旧 `FileService`。新自动配置类 `OssStoreAutoConfiguration` 只在 `fons4cloud.upload.oss.enabled=true` 时生效，并基于 `CloudSecret.provider` 创建 Ali 或 MinIO provider。

共享抽象基类 `AbstractOssStoreService` 负责配置校验、object key 生成、accessUri 解析、访问 URL 拼接和通用异常转换。Ali/MinIO provider 仅负责 SDK 调用。

## 关键规则代码片段

```java
if (!cloudSecret.isEnabled()) {
    // 不注册 OssStoreService
}

String objectKey = StringUtils.isNotBlank(request.getObjectKey())
        ? normalize(request.getObjectKey())
        : buildObjectKey(today, request.getScene(), request.getAccessUniqueId(), generatedFilename);
```

```java
String resolveObjectKey(OssObjectRequest request) {
    if (StringUtils.isNotBlank(request.getObjectKey())) {
        return normalize(request.getObjectKey());
    }
    return removeBucketPrefix(removeEndpointPrefix(request.getAccessUri()));
}
```

## 状态流转设计

- 自动配置状态: 未启用 -> 不注册；启用且配置完整 -> 注册 provider；启用但配置缺失 -> 启动失败。
- 对象状态: 上传后可存在；删除后不存在；对象信息查询依赖 provider 的 stat/head 元数据。

## 数据结构变更

- `CloudSecret`: 新增 `Boolean enabled = false` 和 `ServerProvider provider = ALI_OSS`。
- `ServerProvider`: 新增 `MINIO`。
- 新增 `OssUploadRequest`、`OssObjectRequest`、`OssObjectResponse`，均无持久化。
- DDL file action: none。
- SQL DDL files: none。

## API 与契约细节

- `OssStoreService`:
  - `OssObjectResponse upload(OssUploadRequest request)`
  - `OssObjectResponse download(OssObjectRequest request)`
  - `boolean exists(OssObjectRequest request)`
  - `void delete(OssObjectRequest request)`
  - `OssObjectResponse getObjectInfo(OssObjectRequest request)`
  - `String getAccessUrl(OssObjectRequest request)`
- `OssUploadRequest.objectKey` 优先；为空时按日期、场景、可选唯一 ID、UUID 文件名生成。
- `OssObjectRequest.objectKey` 优先；为空时从 `accessUri` 解析。
- 异常统一转换为 `FileException`，错误消息使用通用文件模块错误码，不拼接密钥值。

## 事务与一致性

- 不涉及数据库事务。
- OSS 操作为外部对象存储调用，不提供跨对象事务。
- 删除、存在判断、元数据查询遵循 provider SDK 的一致性语义。

## 风险与回滚

- Risk: MinIO 依赖冲突。
  - Mitigation: 文件模块测试和编译验证。
  - Rollback: 移除 MinIO provider、依赖和 `MINIO` provider 分支。
- Risk: 新自动配置影响旧应用。
  - Mitigation: `enabled=false` 默认关闭，并测试未启用时不注册新 Bean。
  - Rollback: 从 imports 移除 `OssStoreAutoConfiguration`。
- Risk: 现有知识库旧记录误导。
  - Mitigation: 实施后更新技术架构记录，说明新能力独立于旧 `FileService`。

## AC 映射

| AC | 设计覆盖 |
| --- | --- |
| AC-001 | `OssStoreService` 定义六个对象级方法 |
| AC-002 | 不修改旧 `FileService` 和旧实现文件 |
| AC-003 | `AbstractOssStoreService` 上传时 objectKey 优先 |
| AC-004 | `AbstractOssStoreService` 生成默认 object key |
| AC-005 | `AbstractOssStoreService` 解析 accessUri |
| AC-006 | `@ConditionalOnProperty` 控制新自动配置 |
| AC-007 | `CloudSecret.provider` 默认 Ali |
| AC-008 | `ServerProvider.MINIO` 分支创建 MinIO provider |
| AC-009 | `CloudSecret` 必填校验失败时启动失败 |
| AC-010 | `@ConditionalOnMissingBean(OssStoreService.class)` 保护用户 Bean |
| AC-011 | Ali/MinIO 上传请求写入 metadata |
| AC-012 | provider 构造期创建 SDK 客户端并复用 |
| AC-013 | provider catch SDK 异常后转为 `FileException` |
| AC-014 | 不触碰数据库、DDL、Controller、业务模块 |

## 验证策略

- 自动配置测试覆盖 enabled 默认关闭、Ali 默认、MinIO、配置缺失、自定义 Bean。
- 核心测试覆盖 objectKey 优先、默认 key 生成、accessUri 解析和 metadata 传递。
- Provider 测试用 mock SDK 覆盖 Ali/MinIO 六个对象能力。
- 模块回归运行 `mvn -pl fons4cloud-common/fons4cloud-common-file test`，使用 Java 21。

## 知识同步影响

- Business architecture: no。
- Technical architecture: yes，需记录独立 `OssStoreService` 与旧 `FileService` 的边界。
- Data architecture: no。
- SQL DDL update needed: no。
- Knowledge Sync Needed: yes。
