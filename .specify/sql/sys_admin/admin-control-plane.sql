CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '认证服务账户ID；admin 不保存 source_client_id，统一通过 ADMIN 客户端边界接入',
  `username` varchar(64) NOT NULL COMMENT '账户用户名快照；用于列表展示和登录后审计回显',
  `display_name` varchar(128) DEFAULT NULL COMMENT '管理员展示名称',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '管理员状态：ACTIVE=启用，DISABLED=禁用',
  `last_access_at` datetime DEFAULT NULL COMMENT '最近一次访问时间；用于展示，不作为默认索引维度',
  `last_access_ip` varchar(64) DEFAULT NULL COMMENT '最近一次访问IP；用于审计展示',
  `description` varchar(512) DEFAULT NULL COMMENT '管理员绑定说明',
  `created_by` varchar(128) NOT NULL COMMENT '创建人账号或系统标识',
  `updated_by` varchar(128) DEFAULT NULL COMMENT '最近更新人账号或系统标识',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_account_id` (`account_id`),
  KEY `idx_admin_user_username` (`username`),
  KEY `idx_admin_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 管理员绑定表';

CREATE TABLE `admin_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码；作为授权和初始化的稳定标识',
  `role_name` varchar(128) NOT NULL COMMENT '角色名称',
  `role_type` varchar(32) NOT NULL DEFAULT 'CUSTOM' COMMENT '角色类型：BUILT_IN=内置，CUSTOM=自定义',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态：ACTIVE=启用，DISABLED=禁用',
  `description` varchar(512) DEFAULT NULL COMMENT '角色说明',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role_code` (`role_code`),
  KEY `idx_admin_role_type_status` (`role_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 角色表';

CREATE TABLE `admin_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `permission_code` varchar(128) NOT NULL COMMENT '权限编码；格式由 admin API 权限常量约束',
  `domain` varchar(32) NOT NULL COMMENT '治理域：如网关路由、流量治理、授权资源、OAuth客户端、观测探测',
  `action` varchar(32) NOT NULL COMMENT '操作类型：READ=读取，DRAFT=草稿，VALIDATE=校验，PUBLISH=发布，ROLLBACK=回滚，MANAGE=管理',
  `description` varchar(512) DEFAULT NULL COMMENT '权限说明',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '权限状态：ACTIVE=启用，DISABLED=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_permission_code` (`permission_code`),
  KEY `idx_admin_permission_domain_action` (`domain`,`action`),
  KEY `idx_admin_permission_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 权限点目录表';

CREATE TABLE `admin_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_user_id` bigint NOT NULL COMMENT '管理员ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `granted_by` varchar(128) NOT NULL COMMENT '授权操作人',
  `granted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_role` (`admin_user_id`,`role_id`),
  KEY `idx_admin_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 管理员角色关系表';

CREATE TABLE `admin_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `granted_by` varchar(128) NOT NULL COMMENT '授权操作人',
  `granted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role_permission` (`role_id`,`permission_id`),
  KEY `idx_admin_role_permission_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 角色权限关系表';

CREATE TABLE `admin_governance_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `domain` varchar(32) NOT NULL COMMENT '治理域',
  `resource_type` varchar(64) NOT NULL COMMENT '资源类型；如 ROUTE、IP_WHITE_LIST、OAUTH_CLIENT',
  `resource_key` varchar(256) NOT NULL COMMENT '资源唯一键；由治理适配器按目标系统规则生成',
  `target_ref` varchar(512) NOT NULL COMMENT '权威目标引用；如 Nacos dataId、Redis key 或认证客户端ID',
  `current_hash` varchar(128) DEFAULT NULL COMMENT '最近确认的目标内容摘要；用于漂移检测',
  `current_snapshot_id` bigint DEFAULT NULL COMMENT '最近一次当前态快照ID',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '治理资源状态：ACTIVE=可治理，DISABLED=停用',
  `description` varchar(512) DEFAULT NULL COMMENT '资源说明',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_gov_resource` (`domain`,`resource_type`,`resource_key`),
  KEY `idx_admin_gov_resource_status` (`status`),
  KEY `idx_admin_gov_resource_snapshot_id` (`current_snapshot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 治理资源登记表';

CREATE TABLE `admin_governance_change` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` bigint NOT NULL COMMENT '治理资源ID',
  `change_no` varchar(64) NOT NULL COMMENT '变更单号；用于外部追踪和幂等查询',
  `change_type` varchar(32) NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型：CREATE=新增，UPDATE=修改，DELETE=删除',
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '变更状态：DRAFT=草稿，VALIDATED=已校验，PUBLISHING=发布中，PUBLISHED=已发布，FAILED=失败，ROLLED_BACK=已回滚',
  `base_hash` varchar(128) DEFAULT NULL COMMENT '创建草稿时读取到的目标摘要；用于发布前漂移检测',
  `content` longtext NOT NULL COMMENT '目标配置内容JSON；允许保存受控配置正文',
  `content_hash` varchar(128) NOT NULL COMMENT '目标配置内容摘要；用于审计和一致性校验',
  `validation_result` longtext COMMENT '校验结果JSON',
  `created_by` varchar(128) NOT NULL COMMENT '草稿创建人',
  `updated_by` varchar(128) DEFAULT NULL COMMENT '草稿最近更新人',
  `description` varchar(1024) DEFAULT NULL COMMENT '变更说明',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_gov_change_no` (`change_no`),
  KEY `idx_admin_gov_change_resource_status` (`resource_id`,`status`),
  KEY `idx_admin_gov_change_creator_status` (`created_by`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 治理变更草稿表';

CREATE TABLE `admin_governance_release` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `change_id` bigint NOT NULL COMMENT '治理变更ID',
  `release_no` varchar(64) NOT NULL COMMENT '发布流水号；用于发布和回滚追踪',
  `release_type` varchar(32) NOT NULL DEFAULT 'PUBLISH' COMMENT '发布类型：PUBLISH=发布，ROLLBACK=回滚',
  `status` varchar(32) NOT NULL DEFAULT 'RUNNING' COMMENT '发布状态：RUNNING=执行中，SUCCESS=成功，FAILED=失败',
  `before_hash` varchar(128) DEFAULT NULL COMMENT '发布前目标摘要',
  `after_hash` varchar(128) DEFAULT NULL COMMENT '发布后目标摘要',
  `operator_id` varchar(128) NOT NULL COMMENT '发布操作人ID',
  `started_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `finished_at` datetime DEFAULT NULL COMMENT '结束时间',
  `error_code` varchar(64) DEFAULT NULL COMMENT '错误码；发布失败时记录',
  `error_message` varchar(1024) DEFAULT NULL COMMENT '脱敏错误摘要',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_gov_release_no` (`release_no`),
  KEY `idx_admin_gov_release_change_id` (`change_id`),
  KEY `idx_admin_gov_release_status_started` (`status`,`started_at`),
  KEY `idx_admin_gov_release_operator_started` (`operator_id`,`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 治理发布记录表';

CREATE TABLE `admin_governance_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` bigint NOT NULL COMMENT '治理资源ID',
  `change_id` bigint DEFAULT NULL COMMENT '关联变更ID；当前态快照可为空',
  `release_id` bigint DEFAULT NULL COMMENT '关联发布记录ID；草稿快照可为空',
  `snapshot_type` varchar(32) NOT NULL COMMENT '快照类型：BASE=基线，BEFORE=发布前，AFTER=发布后，ROLLBACK_SOURCE=回滚来源',
  `content` longtext NOT NULL COMMENT '快照配置内容JSON；仅保存治理所需受控内容',
  `content_hash` varchar(128) NOT NULL COMMENT '快照内容摘要',
  `content_summary` varchar(2048) DEFAULT NULL COMMENT '脱敏内容摘要；用于列表展示和审计',
  `retention_until` datetime DEFAULT NULL COMMENT '建议保留截止时间；用于后续清理任务',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_gov_snapshot_resource_type` (`resource_id`,`snapshot_type`),
  KEY `idx_admin_gov_snapshot_change_id` (`change_id`),
  KEY `idx_admin_gov_snapshot_release_id` (`release_id`),
  KEY `idx_admin_gov_snapshot_retention` (`retention_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 治理快照表';

CREATE TABLE `admin_governance_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `domain` varchar(32) NOT NULL COMMENT '治理域',
  `resource_id` bigint DEFAULT NULL COMMENT '治理资源ID',
  `change_id` bigint DEFAULT NULL COMMENT '治理变更ID',
  `operation` varchar(64) NOT NULL COMMENT '操作类型；如 LOGIN、DRAFT_CREATE、VALIDATE、PUBLISH、ROLLBACK、ROLE_GRANT',
  `operator_id` varchar(128) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) DEFAULT NULL COMMENT '操作人展示名称快照',
  `request_id` varchar(128) DEFAULT NULL COMMENT '请求追踪ID',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `result` varchar(32) NOT NULL COMMENT '操作结果：SUCCESS=成功，FAILED=失败，DENIED=拒绝',
  `detail_summary` varchar(2048) DEFAULT NULL COMMENT '脱敏操作摘要；不得写入 token、clientSecret 或完整配置正文',
  `error_code` varchar(64) DEFAULT NULL COMMENT '错误码',
  `error_message` varchar(1024) DEFAULT NULL COMMENT '脱敏错误摘要',
  `operated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作发生时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0=未删除，1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_gov_audit_domain_time` (`domain`,`operated_at`),
  KEY `idx_admin_gov_audit_operator_time` (`operator_id`,`operated_at`),
  KEY `idx_admin_gov_audit_resource_time` (`resource_id`,`operated_at`),
  KEY `idx_admin_gov_audit_change_id` (`change_id`),
  KEY `idx_admin_gov_audit_request_id` (`request_id`),
  KEY `idx_admin_gov_audit_result_time` (`result`,`operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admin 治理审计表';
