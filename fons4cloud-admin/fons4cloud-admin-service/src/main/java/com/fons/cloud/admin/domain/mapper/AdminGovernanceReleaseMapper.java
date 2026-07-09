package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 治理发布记录表 Mapper。
 */
@Mapper
public interface AdminGovernanceReleaseMapper extends BasePlusMapper<AdminGovernanceRelease> {
}
