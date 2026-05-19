-- Database/Service: sys_auth
-- Business Model: transaction_log
-- Tables: tcc_fence_log, undo_log
-- Source: fons4cloud-auth/fons4cloud-auth-service/src/main/resources/sys_auth.sql
-- Status: 已确认
-- Last Generated: 2026-05-15

CREATE TABLE IF NOT EXISTS sys_auth.tcc_fence_log
(
    xid          VARCHAR(128) NOT NULL COMMENT 'global id',
    branch_id    BIGINT       NOT NULL COMMENT 'branch id',
    action_name  VARCHAR(64)  NOT NULL COMMENT 'action name',
    status       TINYINT      NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
    gmt_create   DATETIME(3)  NOT NULL COMMENT 'create time',
    gmt_modified DATETIME(3)  NOT NULL COMMENT 'update time',
    PRIMARY KEY (xid, branch_id)
) CHARSET = utf8mb4;

CREATE INDEX idx_gmt_modified ON sys_auth.tcc_fence_log (gmt_modified);
CREATE INDEX idx_status ON sys_auth.tcc_fence_log (status);

CREATE TABLE sys_auth.undo_log
(
    branch_id     BIGINT       NOT NULL COMMENT 'branch transaction id',
    xid           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB     NOT NULL COMMENT 'rollback info',
    log_status    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    log_created   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    log_modified  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
) COMMENT 'AT transaction mode undo table' CHARSET = utf8mb4;
