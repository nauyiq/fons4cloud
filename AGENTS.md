# 项目级 AI 开发规则

本项目是 fons4cloud 框架层项目。Codex、Cursor、Claude 等 AI 助手在本仓库内进行需求分析、设计、任务拆解、编码、重构、评审和文档更新时，必须优先读取并遵循以下长期规范：

1. `.specify/memory/constitution.md`
2. `.specify/memory/java-development-standard.md`
3. 当前 feature 按 SDD 等级要求生成的产物：S0 读取 `spec.md`；S1 读取 `spec.md`、`plan.md`、`tasks.md`；S2 读取完整 SDD 产物
4. 被修改代码的相关调用方、被调用方、配置、Mapper XML、POM 和测试

## 强制规则

1. 所有回复、设计说明、任务清单、检查清单和项目内新增文档必须使用中文。
2. 修改代码前必须先阅读相关文件，理解上下文后再动手。
3. 删除或修改已有代码前必须先向用户确认，除非用户当前请求已经明确要求该删除或修改。
4. 优先复用项目已有工具函数、组件、starter、公共模块和基础设施，不重复造轮子。
5. 框架层和业务层 Java 代码都必须遵循 `.specify/memory/java-development-standard.md`。
6. 工作树中已有未提交改动必须保留，不得回滚、覆盖或混入无关修改。
7. 所有需求、修复和重构必须先判定 SDD 等级：S0 轻量变更、S1 标准功能、S2 复杂能力；按等级裁剪产物，不得让小变更默认承担完整 SDD 成本。
8. 本项目使用 SDD 驱动进行需求澄清、规格生成、技术方案、任务拆解、实现、分析或 Git 集成时，必须优先使用项目内 `.agents/skills/speckit-*` 系列 skill。全局 skill（如需求澄清、bug 修复、功能实现等）允许作为补充参考或通用流程辅助，但不得替代、绕过或覆盖项目内 SDD skill 与 `.specify` 规范。

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current SDD
artifacts required by the feature level and the project constitution
before implementation.
<!-- SPECKIT END -->
