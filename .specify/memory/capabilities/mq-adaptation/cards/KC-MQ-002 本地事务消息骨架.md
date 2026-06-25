# KC-MQ-002 本地事务消息骨架

> 知识编号：KC-MQ-002
> 知识类型：技术流程
> 所属能力：MQ 抽象与多中间件适配（mq-adaptation）
> 状态：已验证
> 来源：code
> 可信度说明：来自 `fons4cloud-mq-api` 事务消息接口和抽象服务源码；DDL 和补偿任务未确认。
> 关联能力：事务消息
> 关联适配：Kafka、RabbitMQ
> 关联场景：CS-MQ-004
> 关联对象：`MqTransactionalService`、`MqMessageOperations`、`LocalTransactionalMessage`
> 关联代码/接口/SQL：`fons4cloud-mq/fons4cloud-mq-api/src/main/java/com/fons/cloud/mq/api/transactional/service/`
> 更新日期：2026-06-25

## 1. 事实描述

- 核心事实：本地事务消息骨架由 `MqTransactionalService.saveAndSendLocalMessage` 定义，`AbstractMqTransactionalService` 实现“校验消息 -> 保存本地消息 -> 同步发送 MQ -> 失败抛异常”的流程。
- 事实粒度：单一技术流程。
- 适用范围：当前可从 Kafka 和 RabbitMQ 事务服务实现看到该骨架。
- 不适用范围：不确认本地消息表 DDL、补偿扫描任务、消息确认状态机和重试策略。
- 证据依据：`MqTransactionalService.java`、`AbstractMqTransactionalService.java`、`MqMessageOperations.java`、`LocalTransactionalMessage.java`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 消息校验 | 事务消息 | Kafka/RabbitMQ | `messageId` 和 `topic` 不能为空。 | 已验证 |
| 本地保存 | 事务消息 | Kafka/RabbitMQ | 发送 MQ 前调用 `MqMessageOperations.saveMqMessage`。 | 已验证 |
| MQ 发送 | 事务消息 | Kafka/RabbitMQ | 使用公共 `StreamProducer.syncSend` 发送。 | 已验证 |
| 补偿确认 | 事务消息 | 全部 | 只定义查询未确认消息和确认消息接口，未见完整调度实现。 | 待确认 |

## 3. 技术落地

- 入口：`MqTransactionalService.saveAndSendLocalMessage`
- 应用服务：业务事务服务或消息发布服务
- 领域对象/方法：不适用
- 仓储/Mapper：`MqMessageOperations` 由接入方提供实现
- 外部协作：Kafka/RabbitMQ Producer、本地消息存储
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../MQ抽象与多中间件适配能力文档.md`
- 运行文档：`../MQ抽象与多中间件适配运行文档.md`
- 相关卡片：`KC-MQ-001 公共 Stream 发送抽象.md`
