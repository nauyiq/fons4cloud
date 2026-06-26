# KC-GW-005 HTTP 节流与黑白名单

> 知识编号：KC-GW-005
> 知识类型：能力适配
> 所属领域：网关服务（gateway-service）
> 状态：已验证
> 来源：code/user
> 可信度说明：来自节流过滤器、节流器和执行器源码。
> 关联能力：HTTP 节流风控
> 关联适配：自定义 HTTP 节流/风控适配
> 关联场景：CS-GW-007
> 关联对象：`GlobalHttpThrottleFilter`、`GatewayHttpThrottles`、`ThrottlesProcess`
> 关联代码/接口/SQL：`fons4cloud-gateway/src/main/java/com/fons/cloud/gateway/filter/GlobalHttpThrottleFilter.java`
> 更新日期：2026-06-26

## 1. 事实描述

- 核心事实：网关自定义 HTTP 节流链路先放行静态资源、OPTIONS、白名单 URI 和白名单 IP，再执行黑名单、攻击特征和访问频控判定。
- 事实粒度：单一能力适配。
- 适用范围：进入 `GlobalHttpThrottleFilter` 的普通 HTTP 请求。
- 不适用范围：不定义 limiter 内部阈值、算法参数和生产开关值。
- 证据依据：`GlobalHttpThrottleFilter`、`GatewayHttpThrottles`、`ThrottlesProcess`。

## 2. 规则、流程或数据变化

| 项目 | 适用能力 | 适用适配 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 白名单放行 | HTTP 节流 | 自定义节流 | 静态资源、OPTIONS、白名单 URI、白名单 IP 直接进入后续链路。 | 已验证 |
| 黑名单判定 | HTTP 节流 | 自定义节流 | 手工黑名单和行为黑名单优先返回限流结果。 | 已验证 |
| 攻击识别 | HTTP 节流 | 自定义节流 | Body、查询参数和 URI 命中攻击词后写入手工黑名单。 | 已验证 |
| 频控 | HTTP 节流 | 自定义节流 | 调用 `HttpAccessFlowControlCenter.needLimitPerTimeWindow`。 | 已验证 |

## 3. 技术落地

- 入口：Gateway `GlobalFilter`。
- 应用服务：`GatewayHttpThrottles`、`ThrottlesProcess`。
- 领域对象/方法：`HttpRequestInfo`、`LimitResult`、`FlowResult`。
- 仓储/Mapper：无。
- 外部协作：limiter、Redis。
- 测试：本轮未发现测试。

## 4. 关联知识

- 业务文档：无。
- 技术文档：`.specify/memory/capabilities/gateway-service/adaptations/http-throttle-HTTP节流风控能力适配说明.md`
- 数据文档：`.specify/memory/capabilities/gateway-service/网关服务配置与资源文档.md`
- 相关卡片：`KC-GW-004 请求体重复读取包装`
