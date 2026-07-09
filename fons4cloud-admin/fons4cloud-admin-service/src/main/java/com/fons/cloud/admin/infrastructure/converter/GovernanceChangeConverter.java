package com.fons.cloud.admin.infrastructure.converter;

import com.fons.cloud.admin.api.response.GovernanceChangeResponse;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 治理变更响应转换器，避免 Controller 和应用服务重复搬运字段。
 */
@Mapper
public interface GovernanceChangeConverter {

    GovernanceChangeConverter CONVERTER = Mappers.getMapper(GovernanceChangeConverter.class);

    /**
     * 将治理变更实体转换为对外响应。
     *
     * @param change 治理变更实体
     * @return 治理变更响应
     */
    GovernanceChangeResponse mapToResponse(AdminGovernanceChange change);
}
