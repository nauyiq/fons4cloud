package com.fons.cloud.admin.domain.mapper;

import com.fons.cloud.admin.domain.entity.AdminGovernanceAudit;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 治理审计表 Mapper。
 */
@Mapper
public interface AdminGovernanceAuditMapper extends BasePlusMapper<AdminGovernanceAudit> {
}
