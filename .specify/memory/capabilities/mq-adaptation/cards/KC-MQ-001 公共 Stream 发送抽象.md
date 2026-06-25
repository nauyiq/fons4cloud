# KC-MQ-001 公共 Stream 发送抽象

> 知识编号：KC-MQ-001
> 知识类型：接口契约
> 所属能力：MQ 抽象与多中间件适配（mq-adaptation）
> 状态：已验证
> 来源：code
> 可信度说明：来自公共接口和抽象模板源码。
> 关联能力：消息发送
> 关联适配：全部 MQ Provider
> 关联场景：CS-MQ-001、CS-MQ-002、CS-MQ-003
> 关联对象：`StreamProducer`、`StreamMessage`、`AbstractStreamProducerTemplate`
> 关联代码/接口/SQL：`fons4cloud-common/fons4cloud-common-stream/src/main/java/com/fons/cloud/stream/api/`
> 更新日期：2026-06-25

## 1. 事实描述

- 核心事实：公共 Stream 发送抽象由 `StreamProducer`、`StreamMessage`、`StreamProducerFactory` 和 `AbstractStreamProducerTemplate` 组成，统一定义同步、异步、oneway 发送骨架。
- 事实粒度：单一接口契约。
- 适用范围：Kafka、RabbitMQ、RocketMQ Template 等继承公共模板的发送能力。
- 不适用范围：不覆盖 `StreamBridgeProducer` 的独立发送路径，不覆盖 Provider 特定资源模型。
- 证据依据：`StreamProducer.java`、`StreamMessage.java`、`AbstractStreamProducerTemplate.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 发送模式 | 消息发送 | 全部公共 Producer | 标准骨架包含 sync、async、oneway。 | 已验证 |
| 消息校验 | 消息发送 | 全部公共 Producer | 消息对象和 Topic 不合法时返回错误结果。 | 已验证 |
| Provider 选择 | 消息发送 | Kafka/RabbitMQ/RocketMQ | 公共模板不负责 Provider 自动选择，具体 Bean 由适配配置注册。 | 已验证 |

## 3. 技术落地

- 入口：`StreamProducer.syncSend`、`StreamProducer.asyncSend`、`StreamProducer.onewaySend`
- 应用服务：接入方应用服务
- 领域对象/方法：不适用
- 仓储/Mapper：不适用
- 外部协作：Kafka/RabbitMQ/RocketMQ 客户端
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../MQ抽象与多中间件适配能力文档.md`
- 运行文档：`../MQ抽象与多中间件适配运行文档.md`
- 配置与资源文档：`../MQ抽象与多中间件适配配置与资源文档.md`
- 相关卡片：`KC-MQ-003 MQ Provider 适配差异.md`
