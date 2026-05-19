-- Database/Service: seata
-- Business Model: transaction_client
-- Tables: undo_log, tcc_fence_log
-- Source: fons4cloud-common/fons4cloud-common-seata/db/seata.sql
-- Status: 已确认
-- Last Generated: 2026-05-15

CREATE TABLE `undo_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `branch_id` BIGINT(20) NOT NULL,
    `xid` VARCHAR(100) NOT NULL,
    `context` VARCHAR(128) NOT NULL,
    `rollback_info` LONGBLOB NOT NULL,
    `log_status` INT(11) NOT NULL,
    `log_created` DATETIME NOT NULL,
    `log_modified` DATETIME NOT NULL,
    `ext` VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tcc_fence_log`
(
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global id',
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch id',
    `action_name`   VARCHAR(64)  NOT NULL COMMENT 'action name',
    `status`        TINYINT      NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
    `gmt_create`    DATETIME(3)  NOT NULL COMMENT 'create time',
    `gmt_modified`  DATETIME(3)  NOT NULL COMMENT 'update time',
    PRIMARY KEY (`xid`, `branch_id`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
