# KC-MQ-003 MQ Provider 适配差异

> 知识编号：KC-MQ-003
> 知识类型：能力适配
> 所属能力：MQ 抽象与多中间件适配（mq-adaptation）
> 状态：已验证
> 来源：code/config
> 可信度说明：来自三类 Provider 的消息模型、Producer、AutoConfiguration 和配置文件。
> 关联能力：消息发送、动态资源初始化
> 关联适配：Kafka、RabbitMQ、RocketMQ Template、RocketMQ StreamBridge
> 关联场景：CS-MQ-001、CS-MQ-005、CS-MQ-007
> 关联对象：`KafkaProducer`、`RabbitmqProducer`、`RocketmqProducer`、`StreamBridgeProducer`
> 关联代码/接口/SQL：`fons4cloud-mq/fons4cloud-mq-*`
> 更新日期：2026-06-25

## 1. 事实描述

- 核心事实：Kafka、RabbitMQ、RocketMQ 使用不同消息模型和目标资源，不能把任一 Provider 的发送细节写成全局标准。
- 事实粒度：单一适配差异。
- 适用范围：MQ Provider 选型、接入和适配说明。
- 不适用范围：不提供业务 Topic 选型规则，不确认生产集群资源治理。
- 证据依据：`KafkaStreamMessage.java`、`RabbitMessage.java`、`RocketmqMessage.java`、三类 Producer 和 AutoConfiguration。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 目标资源 | 消息发送 | Kafka | 使用 Topic 和可选 partition。 | 已验证 |
| 目标资源 | 消息发送 | RabbitMQ | 使用 exchange、routingKey、Queue、Binding。 | 已验证 |
| 目标资源 | 消息发送 | RocketMQ Template | 使用 topic、tags、destination、orderly hash。 | 已验证 |
| 目标资源 | 消息发送 | RocketMQ StreamBridge | 使用 bindingName、tag、headers。 | 已验证 |
| 动态资源 | 资源初始化 | RabbitMQ | 支持动态声明 Queue/Exchange/Binding。 | 已验证 |
| 动态资源 | 资源初始化 | Kafka | 配置对象存在，但创建 Topic 的逻辑被注释。 | 待确认 |

## 3. 技术落地

- 入口：各 Provider Producer
- 应用服务：接入方应用服务
- 领域对象/方法：不适用
- 仓储/Mapper：不适用
- 外部协作：Kafka Broker、RabbitMQ Broker、RocketMQ NameServer/Broker
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 适配矩阵：`../能力适配矩阵.md`
- Kafka 适配说明：`../adaptations/kafka-消息发送与事务消息能力适配说明.md`
- RabbitMQ 适配说明：`../adaptations/rabbitmq-动态队列交换机与消息发送能力适配说明.md`
- RocketMQ 适配说明：`../adaptations/rocketmq-template与streambridge双路径能力适配说明.md`
