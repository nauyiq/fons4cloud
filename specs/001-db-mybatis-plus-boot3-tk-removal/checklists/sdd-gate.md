# Checklist: SDD 补救门禁

**Feature**: db 模块 MyBatis-Plus Boot3 Starter 迁移与 TK Mapper 移除  
**Date**: 2026-04-30  
**SDD Level**: S2

## 规范读取

- [x] 已读取 `.specify/memory/constitution.md`
- [x] 已读取 `.specify/memory/java-development-standard.md`
- [x] 已读取 `AGENTS.md`
- [x] 已读取被修改代码相关 POM、配置、Java 类和 Mapper XML

## SDD 流程补救

- [x] 已补判 SDD 等级为 S2
- [x] 已记录为何不是 S0/S1
- [x] 已记录前一轮未按 SDD 流程推进的问题
- [x] 已补齐 `spec.md`
- [x] 已补齐 `research.md`
- [x] 已补齐 `plan.md`
- [x] 已补齐 `tasks.md`
- [x] 已补齐兼容性契约说明
- [x] 已补齐验证 quickstart

## 变更授权

- [x] 用户已明确允许删除 TK Mapper 依赖
- [x] 用户已明确允许删除 `com.fons.cloud.db.tk` 整包
- [x] 用户已明确接受对外不兼容变更

## 技术验证

- [x] db-core 构建通过
- [x] db-shardingsphere 下游链路构建通过
- [x] 依赖树确认使用 `mybatis-plus-spring-boot3-starter`
- [x] 依赖树确认无 `tk.mybatis`
- [x] 源码扫描确认无 TK Mapper 相关残留
- [x] 记录 Maven 默认 JDK 8 导致的 Java 21 构建失败事实
- [x] 记录 `-DskipTests` 跳过测试事实

## 残余风险

- [ ] 尚未执行全量业务模块编译
- [ ] 尚未执行自动化测试
- [ ] 尚未单独评估 PageHelper 升级

