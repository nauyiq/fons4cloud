package com.fons.cloud.admin.infrastructure.converter;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.api.request.GovernanceDraftCreateRequest;
import com.fons.cloud.admin.api.request.GroupedGovernanceDraftCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 治理草稿请求转换器。
 */
@Mapper
public interface GovernanceDraftRequestConverter {

    GovernanceDraftRequestConverter CONVERTER = Mappers.getMapper(GovernanceDraftRequestConverter.class);

    /**
     * 将分组治理请求转换为通用治理草稿请求。
     *
     * @param request      分组治理请求
     * @param domain       REST 路径绑定的治理能力域
     * @param resourceType REST 路径绑定的资源类型
     * @return 通用治理草稿请求
     */
    @Mapping(target = "domain", source = "domain")
    @Mapping(target = "resourceType", source = "resourceType")
    @Mapping(target = "resourceKey", source = "request.resourceKey")
    @Mapping(target = "baseHash", source = "request.baseHash")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "changeType", source = "request.changeType")
    @Mapping(target = "description", source = "request.description")
    GovernanceDraftCreateRequest mapToCreateRequest(GroupedGovernanceDraftCreateRequest request,
                                                    GovernanceDomain domain,
                                                    String resourceType);
}
