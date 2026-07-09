package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminRolePermission;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 角色权限关系表 Mapper。
 */
@Mapper
public interface AdminRolePermissionMapper extends BasePlusMapper<AdminRolePermission> {
}
