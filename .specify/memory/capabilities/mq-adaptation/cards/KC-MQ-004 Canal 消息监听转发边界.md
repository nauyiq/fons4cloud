# KC-MQ-004 Canal 消息监听转发边界

> 知识编号：KC-MQ-004
> 知识类型：治理规则
> 所属能力：MQ 抽象与多中间件适配（mq-adaptation）
> 状态：已验证
> 来源：code/user
> 可信度说明：来自 Kafka/Rabbit/Rocket Canal Listener 源码和本轮用户确认。
> 关联能力：Canal 消息监听转发
> 关联适配：Kafka、RabbitMQ、RocketMQ
> 关联场景：CS-MQ-006
> 关联对象：`DefaultKafkaCanalListener`、`RabbitCanalListener`、`RocketCanalListener`、`CanalGlue`
> 关联代码/接口/SQL：`fons4cloud-mq/fons4cloud-mq-*/src/main/java/com/fons/cloud/mq/*/canal/`
> 更新日期：2026-06-25

## 1. 事实描述

- 核心事实：Canal 消息监听转发横跨 Kafka、RabbitMQ、RocketMQ，但它不是新的 MQ Provider；Listener 收到消息后都交给 `CanalGlue.process`，核心解析规则属于 `fons4cloud-common-canal`。
- 事实粒度：单一边界规则。
- 适用范围：MQ 能力域的 Canal 监听转发描述。
- 不适用范围：不展开 Canal 字段解析、表模型、binlog 事件转换和业务处理规则。
- 证据依据：`DefaultKafkaCanalListener.java`、`RabbitCanalListener.java`、`RocketCanalListener.java`、用户确认。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| Provider 边界 | Canal 监听转发 | 全部 | Canal 监听转发作为跨适配场景，不单独作为 MQ Provider。 | 已确认 |
| Kafka Ack | Canal 监听转发 | Kafka | 批量处理后调用 `acknowledgment.acknowledge()`。 | 已验证 |
| Rabbit Ack | Canal 监听转发 | RabbitMQ | 成功 `basicAck`，失败 `basicNack` 且不重回队列。 | 已验证 |
| Rocket 监听 | Canal 监听转发 | RocketMQ | 子类定义 `@RocketMQMessageListener` 后调用 `CanalGlue.process`。 | 已验证 |

## 3. 技术落地

- 入口：Kafka/Rabbit/Rocket Canal Listener
- 应用服务：Canal 消息处理链路
- 领域对象/方法：不适用
- 仓储/Mapper：不适用
- 外部协作：`CanalGlue`
- 测试：未发现当前能力稳定测试文件

## 4. 关联知识

- 能力文档：`../MQ抽象与多中间件适配能力文档.md`
- 适配矩阵：`../能力适配矩阵.md`
- 后续建议：单独建模 `common-canal` 能力时展开解析规则。
