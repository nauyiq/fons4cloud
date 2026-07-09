package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminRole;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 角色表 Mapper。
 */
@Mapper
public interface AdminRoleMapper extends BasePlusMapper<AdminRole> {
}
