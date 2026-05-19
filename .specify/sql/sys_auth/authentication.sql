-- Database/Service: sys_auth
-- Business Model: authentication
-- Tables: account, oauth_client
-- Source: fons4cloud-auth/fons4cloud-auth-service/src/main/resources/sys_auth.sql
-- Status: 已确认
-- Last Generated: 2026-05-15

CREATE TABLE IF NOT EXISTS sys_auth.account
(
    id        BIGINT               NOT NULL COMMENT 'id' PRIMARY KEY,
    client_id VARCHAR(64)          NOT NULL COMMENT '用于唯一标识每一个客户端(client)',
    username  VARCHAR(16)          NOT NULL COMMENT '用户名',
    phone     VARCHAR(64)          NULL COMMENT '手机',
    password  VARCHAR(64)          NULL COMMENT '密码,密文保存',
    email     VARCHAR(64)          NULL COMMENT '用户邮箱',
    id_card   VARCHAR(64)          NULL COMMENT '身份证',
    real_name VARCHAR(64)          NULL COMMENT '真实姓名',
    role      VARCHAR(16)          NULL COMMENT '用户角色',
    status    VARCHAR(16)          NOT NULL,
    version   INT        DEFAULT 0 NULL COMMENT '锁版本',
    deleted   TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除',
    created   DATETIME             NULL COMMENT '创建时间',
    updated   DATETIME             NOT NULL COMMENT '更新时间',
    CONSTRAINT udx_email UNIQUE (email, deleted),
    CONSTRAINT udx_phonez UNIQUE (phone, deleted),
    CONSTRAINT udx_username UNIQUE (username, deleted)
) COMMENT '基础账户表';

CREATE TABLE IF NOT EXISTS sys_auth.oauth_client
(
    client_id               VARCHAR(64)          NOT NULL COMMENT '用于唯一标识每一个客户端(client)' PRIMARY KEY,
    resource_ids            VARCHAR(256)         NULL COMMENT '客户端所能访问的资源id集合,多个资源时用逗号(,)分隔.',
    client_secret           VARCHAR(256)         NULL COMMENT '用于指定客户端(client)的访问密匙; 在注册时必须填写(也可由服务端自动生成).',
    scope                   VARCHAR(256)         NULL COMMENT '指定客户端申请的权限范围.',
    authorized_grant_types  VARCHAR(256)         NULL COMMENT '指定客户端支持的grant_type.',
    web_server_redirect_uri VARCHAR(256)         NULL COMMENT '客户端的重定向URI.',
    authorities             VARCHAR(256)         NULL COMMENT '指定客户端所拥有的Spring Security的权限值',
    access_token_validity   INT                  NULL COMMENT 'access_token有效时间,单位秒.',
    refresh_token_validity  INT                  NULL COMMENT 'refresh_token有效时间,单位秒.',
    additional_information  VARCHAR(4096)        NULL COMMENT '预留字段,必须是JSON格式的数据',
    autoapprove             VARCHAR(256)         NULL COMMENT '用户是否自动Approval操作',
    status                  TINYINT(1) DEFAULT 1 NOT NULL COMMENT '状态 是否可用',
    deleted                 TINYINT(1) DEFAULT 0 NOT NULL COMMENT '状态 是否删除',
    version                 INT        DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
    created                 DATETIME             NULL COMMENT '创建时间',
    updated                 DATETIME             NULL COMMENT '更新时间'
) COMMENT 'oauth2商户客户端表';
