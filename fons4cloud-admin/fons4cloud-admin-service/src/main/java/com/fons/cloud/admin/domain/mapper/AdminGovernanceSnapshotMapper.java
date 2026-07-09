package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 治理快照表 Mapper。
 */
@Mapper
public interface AdminGovernanceSnapshotMapper extends BasePlusMapper<AdminGovernanceSnapshot> {
}
