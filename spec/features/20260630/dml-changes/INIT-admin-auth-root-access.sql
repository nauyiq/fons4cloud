-- 框架控制面 sys-admin 认证客户端与 ROOT 管理员初始化 DML 草稿。
-- 执行前提：
-- 1. 仅供联调或测试环境由用户/DBA 审核后手动执行；不得直接在生产环境盲目执行。
-- 2. `@admin_client_secret_hash` 必须使用认证服务当前 `PasswordEncoder` 生成，当前代码为 BCryptPasswordEncoder。
-- 3. `@admin_account_password_hash` 必须使用同一 PasswordEncoder 生成，明文密码不得写入脚本、日志或提交记录。
-- 4. admin-service 配置项 `admin.auth.client-secret` 的明文必须与 `@admin_client_secret_hash` 对应。
-- 5. `@admin_account_id` 必须替换为目标环境未占用的认证账号 ID；推荐通过正式账号注册流程生成后再绑定。
-- 6. 本脚本会写入 `sys_auth.oauth_client`、`sys_auth.account` 和 `sys_admin` 权限表；执行前请备份目标库。
--
-- 回滚说明：
-- 1. 若只用于联调，可按脚本末尾“回滚 SQL 草稿”先删除 admin 绑定，再删除 sys-admin 账号和客户端。
-- 2. 若已产生审计、发布、快照或 token，请先停用账号和客户端，再由 DBA 确认数据保留要求。

START TRANSACTION;

-- ===== 必须按目标环境替换的变量 =====
SET @admin_client_id = 'sys-admin';
SET @admin_client_secret_hash = '$2a$10$C55nHocmBNzEWoY7DT60deUl7wM3JuP3x90MQ9T8qtfWiSNh1gqHG';
SET @admin_account_id = 202607070001;
SET @admin_username = 'admin_root';
SET @admin_display_name = 'ROOT管理员';
SET @admin_account_password_hash = '$2a$10$C55nHocmBNzEWoY7DT60deUl7wM3JuP3x90MQ9T8qtfWiSNh1gqHG';
SET @operator = 'system';

-- ===== 认证服务：sys-admin OAuth Client =====
INSERT INTO sys_auth.oauth_client (
  client_id,
  resource_ids,
  client_secret,
  scope,
  authorized_grant_types,
  web_server_redirect_uri,
  authorities,
  access_token_validity,
  refresh_token_validity,
  additional_information,
  autoapprove,
  status,
  deleted,
  version,
  created,
  updated
) VALUES (
  @admin_client_id,
  'all',
  @admin_client_secret_hash,
  'all',
  'password,sms,refresh_token',
  NULL,
  'ADMIN',
  259200,
  2592000,
  '{}',
  'true',
  1,
  0,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  resource_ids = VALUES(resource_ids),
  client_secret = IF(client_secret IS NULL OR client_secret = '' OR deleted = 1, VALUES(client_secret), client_secret),
  scope = VALUES(scope),
  authorized_grant_types = VALUES(authorized_grant_types),
  authorities = VALUES(authorities),
  access_token_validity = VALUES(access_token_validity),
  refresh_token_validity = VALUES(refresh_token_validity),
  additional_information = VALUES(additional_information),
  autoapprove = VALUES(autoapprove),
  status = 1,
  deleted = 0,
  updated = NOW();

-- ===== 认证服务：sys-admin 客户端下的 ROOT 账号 =====
INSERT INTO sys_auth.account (
  id,
  client_id,
  username,
  password,
  email,
  phone,
  real_name,
  id_card,
  certification,
  role,
  status,
  deleted,
  version,
  created,
  updated
) VALUES (
  @admin_account_id,
  @admin_client_id,
  @admin_username,
  @admin_account_password_hash,
  NULL,
  NULL,
  NULL,
  NULL,
  0,
  'ADMIN',
  'ACTIVE',
  0,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  client_id = @admin_client_id,
  username = VALUES(username),
  password = IF(password IS NULL OR password = '' OR deleted = 1, VALUES(password), password),
  role = 'ADMIN',
  status = 'ACTIVE',
  deleted = 0,
  updated = NOW();

-- ===== admin 服务：权限点目录 =====
INSERT INTO sys_admin.admin_permission (permission_code, domain, action, description, status, deleted, version, created, updated) VALUES
('services:view', 'services', 'view', '服务治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('gateway:view', 'gateway', 'view', '网关治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('gateway:edit', 'gateway', 'edit', '网关治理编辑草稿', 'ACTIVE', 0, 0, NOW(), NOW()),
('gateway:publish', 'gateway', 'publish', '网关治理发布', 'ACTIVE', 0, 0, NOW(), NOW()),
('gateway:rollback', 'gateway', 'rollback', '网关治理回滚', 'ACTIVE', 0, 0, NOW(), NOW()),
('traffic:view', 'traffic', 'view', '流量治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('traffic:edit', 'traffic', 'edit', '流量治理编辑草稿', 'ACTIVE', 0, 0, NOW(), NOW()),
('traffic:publish', 'traffic', 'publish', '流量治理发布', 'ACTIVE', 0, 0, NOW(), NOW()),
('traffic:rollback', 'traffic', 'rollback', '流量治理回滚', 'ACTIVE', 0, 0, NOW(), NOW()),
('access:view', 'access', 'view', '身份与权限治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('access:edit', 'access', 'edit', '身份与权限治理编辑', 'ACTIVE', 0, 0, NOW(), NOW()),
('access:publish', 'access', 'publish', '身份与权限治理发布', 'ACTIVE', 0, 0, NOW(), NOW()),
('access:rollback', 'access', 'rollback', '身份与权限治理回滚', 'ACTIVE', 0, 0, NOW(), NOW()),
('clients:view', 'clients', 'view', '认证客户端治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('clients:edit', 'clients', 'edit', '认证客户端治理编辑', 'ACTIVE', 0, 0, NOW(), NOW()),
('clients:publish', 'clients', 'publish', '认证客户端治理发布', 'ACTIVE', 0, 0, NOW(), NOW()),
('clients:rollback', 'clients', 'rollback', '认证客户端治理回滚', 'ACTIVE', 0, 0, NOW(), NOW()),
('observability:view', 'observability', 'view', '可观测治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('changes:view', 'changes', 'view', '变更治理查看', 'ACTIVE', 0, 0, NOW(), NOW()),
('changes:edit', 'changes', 'edit', '变更治理编辑草稿', 'ACTIVE', 0, 0, NOW(), NOW()),
('changes:publish', 'changes', 'publish', '变更治理发布', 'ACTIVE', 0, 0, NOW(), NOW()),
('changes:rollback', 'changes', 'rollback', '变更治理回滚', 'ACTIVE', 0, 0, NOW(), NOW()),
('audits:view', 'audits', 'view', '审计查询查看', 'ACTIVE', 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  domain = VALUES(domain),
  action = VALUES(action),
  description = VALUES(description),
  status = 'ACTIVE',
  deleted = 0,
  updated = NOW();

-- ===== admin 服务：ROOT 角色与全部权限授权 =====
INSERT INTO sys_admin.admin_role (
  role_code,
  role_name,
  role_type,
  status,
  description,
  deleted,
  version,
  created,
  updated
) VALUES (
  'ADMIN_ROOT',
  '超级管理员',
  'BUILT_IN',
  'ACTIVE',
  '控制面内置超级管理员角色',
  0,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  status = 'ACTIVE',
  description = VALUES(description),
  deleted = 0,
  updated = NOW();

SET @admin_root_role_id = (
  SELECT id
  FROM sys_admin.admin_role
  WHERE role_code = 'ADMIN_ROOT' AND deleted = 0
  LIMIT 1
);

INSERT INTO sys_admin.admin_role_permission (
  role_id,
  permission_id,
  granted_by,
  granted_at,
  deleted,
  version,
  created,
  updated
)
SELECT
  @admin_root_role_id,
  p.id,
  @operator,
  NOW(),
  0,
  0,
  NOW(),
  NOW()
FROM sys_admin.admin_permission p
WHERE p.deleted = 0
  AND p.status = 'ACTIVE'
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_at = VALUES(granted_at),
  deleted = 0,
  updated = NOW();

-- ===== admin 服务：ROOT 管理员绑定 =====
INSERT INTO sys_admin.admin_user (
  account_id,
  username,
  display_name,
  status,
  last_access_at,
  last_access_ip,
  description,
  created_by,
  updated_by,
  deleted,
  version,
  created,
  updated
) VALUES (
  @admin_account_id,
  @admin_username,
  @admin_display_name,
  'ACTIVE',
  NULL,
  NULL,
  '首个 ROOT 管理员初始化',
  @operator,
  @operator,
  0,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  display_name = VALUES(display_name),
  status = 'ACTIVE',
  description = VALUES(description),
  updated_by = @operator,
  deleted = 0,
  updated = NOW();

SET @admin_user_id = (
  SELECT id
  FROM sys_admin.admin_user
  WHERE account_id = @admin_account_id AND deleted = 0
  LIMIT 1
);

INSERT INTO sys_admin.admin_user_role (
  admin_user_id,
  role_id,
  granted_by,
  granted_at,
  deleted,
  version,
  created,
  updated
) VALUES (
  @admin_user_id,
  @admin_root_role_id,
  @operator,
  NOW(),
  0,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_at = VALUES(granted_at),
  deleted = 0,
  updated = NOW();

COMMIT;

-- 执行后核验 SQL：
-- SELECT client_id, scope, authorized_grant_types, authorities, status, deleted
-- FROM sys_auth.oauth_client
-- WHERE client_id = @admin_client_id;
--
-- SELECT id, client_id, username, role, status, deleted
-- FROM sys_auth.account
-- WHERE id = @admin_account_id OR (client_id = @admin_client_id AND username = @admin_username);
--
-- SELECT u.account_id, u.username, u.status, r.role_code, r.status AS role_status, COUNT(rp.permission_id) AS permission_count
-- FROM sys_admin.admin_user u
-- JOIN sys_admin.admin_user_role ur ON ur.admin_user_id = u.id AND ur.deleted = 0
-- JOIN sys_admin.admin_role r ON r.id = ur.role_id AND r.deleted = 0
-- LEFT JOIN sys_admin.admin_role_permission rp ON rp.role_id = r.id AND rp.deleted = 0
-- WHERE u.account_id = @admin_account_id AND u.deleted = 0
-- GROUP BY u.account_id, u.username, u.status, r.role_code, r.status;

-- 回滚 SQL 草稿，使用前必须确认没有需要保留的审计、快照、发布记录和 token：
-- START TRANSACTION;
-- DELETE ur FROM sys_admin.admin_user_role ur
-- JOIN sys_admin.admin_user u ON u.id = ur.admin_user_id
-- WHERE u.account_id = @admin_account_id;
-- DELETE FROM sys_admin.admin_user WHERE account_id = @admin_account_id;
-- DELETE FROM sys_auth.account WHERE id = @admin_account_id AND client_id = @admin_client_id;
-- DELETE rp FROM sys_admin.admin_role_permission rp
-- JOIN sys_admin.admin_role r ON r.id = rp.role_id
-- WHERE r.role_code = 'ADMIN_ROOT';
-- DELETE FROM sys_admin.admin_role WHERE role_code = 'ADMIN_ROOT';
-- DELETE FROM sys_auth.oauth_client WHERE client_id = @admin_client_id;
-- COMMIT;
