# 项目地图

Fons4Cloud 提供可组合的微服务基础库、运行服务和治理控制台；项目定位、架构及构建入口见 [README](../../README.md)。本页是源码导航，不表示所有组件已部署或所有链路已运行验证。根 [POM](../../pom.xml) 列出聚合模块和声明的版本。

| 能力或模块 | 职责与边界 | 代表性代码与测试入口 | 设计资料 |
| --- | --- | --- | --- |
| [认证](../../fons4cloud-auth/) | 公共认证模型、集中授权服务、Spring Security 和独立 Sa-Token 接入 | [SaTokenAuthTemplate](../../fons4cloud-auth/fons4cloud-auth-satoken/src/main/java/com/fons/cloud/auth/satoken/api/SaTokenAuthTemplate.java)；[会话测试](../../fons4cloud-auth/fons4cloud-auth-satoken/src/test/java/com/fons/cloud/auth/satoken/api/SaTokenAuthTemplateTest.java) | [网关与认证](../gateway-auth.md) |
| [网关](../../fons4cloud-gateway/) | 路由、资源鉴权与用户透传等 | [GatewayMain](../../fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/GatewayMain.java)；[权限判定测试](../../fons4cloud-gateway/src/test/java/com/fons/cloud/gateway/server/auth/AuthorizationManagerTest.java) | [网关与认证](../gateway-auth.md) |
| [公共组件](../../fons4cloud-common/) | 基础模型、Web、数据、缓存、流控、消息协议等按模块组合的能力 | [StreamProducer](../../fons4cloud-common/fons4cloud-common-stream/src/main/java/com/fons/cloud/stream/api/StreamProducer.java)；[公共异常](../../fons4cloud-common/fons4cloud-common-base/src/main/java/com/fons/cloud/common/exception/) | [系统架构](../architecture.md)、[消息设计](../messaging.md) |
| [消息适配](../../fons4cloud-mq/) | 消息 API 与 Kafka、RabbitMQ、RocketMQ 适配；各中间件语义仍需分别核实 | [本地消息扩展口](../../fons4cloud-mq/fons4cloud-mq-api/src/main/java/com/fons/cloud/mq/api/transactional/service/MqMessageOperations.java) | [消息设计](../messaging.md) |
| [治理控制台](../../fons4cloud-admin/) | API、治理服务及 Vue 前端；连接实际目标需配置和权限 | [发布服务](../../fons4cloud-admin/fons4cloud-admin-service/src/main/java/com/fons/cloud/admin/application/GovernancePublishService.java)；[发布测试](../../fons4cloud-admin/fons4cloud-admin-service/src/test/java/com/fons/cloud/admin/application/GovernancePublishServiceTest.java)；[前端入口](../../fons4cloud-admin/fons4cloud-admin-ui/src/main.ts) | [治理控制台](../governance.md) |
| [Starter](../../fons4cloud-starter/) | Nacos、Dubbo 接入模块 | [聚合 POM](../../fons4cloud-starter/pom.xml) | [系统架构](../architecture.md) |
| [基础服务](../../fons4cloud-infrastructure/) | 基础服务 API 和短信策略；包含 Mock 短信实现 | [服务入口](../../fons4cloud-infrastructure/fons4cloud-infrastructure-service/src/main/java/com/fons/cloud/infrastructure/Main.java)；[Mock 短信策略](../../fons4cloud-infrastructure/fons4cloud-infrastructure-service/src/main/java/com/fons/cloud/infrastructure/sms/strategt/MockSmsStrategy.java) | [系统架构](../architecture.md) |

[工程约定](工程约定.md) 汇总有证据的局部实践。上表测试是阅读入口，并非本次运行结果；部署拓扑、外部组件配置、完整端到端行为及生产性能需在目标环境验证。README 中的历史文档基线与当前代码可能不同，修改前应重新核对。
