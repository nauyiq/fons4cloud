package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 管理员绑定表 Mapper。
 */
@Mapper
public interface AdminUserMapper extends BasePlusMapper<AdminUser> {
}
