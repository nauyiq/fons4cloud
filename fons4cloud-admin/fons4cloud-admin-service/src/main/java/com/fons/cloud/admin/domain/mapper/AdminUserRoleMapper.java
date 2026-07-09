package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminUserRole;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 管理员角色关系表 Mapper。
 */
@Mapper
public interface AdminUserRoleMapper extends BasePlusMapper<AdminUserRole> {
}
