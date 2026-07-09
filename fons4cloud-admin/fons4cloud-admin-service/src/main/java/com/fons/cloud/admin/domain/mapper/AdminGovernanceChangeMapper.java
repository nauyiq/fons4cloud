package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 治理变更草稿表 Mapper。
 */
@Mapper
public interface AdminGovernanceChangeMapper extends BasePlusMapper<AdminGovernanceChange> {
}
