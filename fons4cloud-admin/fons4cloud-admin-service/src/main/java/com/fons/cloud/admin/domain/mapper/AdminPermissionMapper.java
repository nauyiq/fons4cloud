package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminPermission;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 权限点目录表 Mapper。
 */
@Mapper
public interface AdminPermissionMapper extends BasePlusMapper<AdminPermission> {
}
