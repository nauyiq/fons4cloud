# KC-GW-004 请求体重复读取包装

> 知识编号：KC-GW-004
> 知识类型：技术流程
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/user
> 可信度说明：来自 `WrapperRequestGlobalFilter` 源码和用户确认。
> 关联能力：请求体重复读取包装
> 关联适配：请求体重复读取包装适配
> 关联场景：CS-GW-006
> 关联对象：`WrapperRequestGlobalFilter`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/filter/WrapperRequestGlobalFilter.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关通过 `WrapperRequestGlobalFilter` 将请求体 DataBuffer 缓存为可重复读取的 `ServerHttpRequestDecorator`，供后续过滤器读取 Body。
- 事实粒度：单一技术流程。
- 适用范围：非 OPTIONS 请求且重复读取开关开启。
- 不适用范围：不定义大请求体、文件上传和流式请求的生产排除规则。
- 证据依据：`WrapperRequestGlobalFilter`、`Constants.WRAPPER_REQUEST_FILTER_ORDER`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 开关控制 | 请求包装 | 请求体重复读取 | `ENABLE_REPEAT_READABLE_HTTP_REQUEST_WRAPPER_FILTER` 关闭时跳过。 | 已验证 |
| OPTIONS 跳过 | 请求包装 | 请求体重复读取 | OPTIONS 请求不包装。 | 已验证 |
| 过滤顺序 | 请求包装 | 请求体重复读取 | 顺序为 `Ordered.HIGHEST_PRECEDENCE`。 | 已验证 |

## 3. 技术落地

- 入口：Gateway `GlobalFilter`。
- 应用服务：无。
- 领域对象/方法：`DataBufferUtils.join`、`ServerHttpRequestDecorator`。
- 仓储/Mapper：无。
- 外部协作：WebFlux DataBuffer。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/网关服务运行文档.md`
- 数据文档：无。
- 相关卡片：`KC-GW-005 HTTP 节流与黑白名单`
