package com.fons.cloud.auth.infrastructure.converter;

import com.fons.cloud.auth.domain.entity.OauthClient;
import com.fons.cloud.auth.request.OauthClientCreateRequest;
import com.fons.cloud.auth.request.OauthClientModifyRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.common.base.converter.CommonConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * OAuth Client 管理契约对象转换器。
 * 密钥生成、加密和脱敏不在转换器中处理，避免明文密钥被普通字段映射误用。
 */
@SuppressWarnings("all")
@Mapper(uses = CommonConverter.class,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OauthClientManagementConverter {

    OauthClientManagementConverter CONVERTER = Mappers.getMapper(OauthClientManagementConverter.class);

    /**
     * 将新增请求转换为 OAuth Client 实体，不映射明文密钥。
     *
     * @param request 新增请求
     * @return OAuth Client 实体
     */
    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    OauthClient createEntity(OauthClientCreateRequest request);

    /**
     * 将修改请求合并到已存在的 OAuth Client 实体；请求为空的字段不覆盖原值。
     *
     * @param request 修改请求
     * @param client  已存在实体
     */
    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    void updateEntity(OauthClientModifyRequest request, @MappingTarget OauthClient client);

    /**
     * 将 OAuth Client 实体转换为脱敏响应视图，不包含 clientSecret。
     *
     * @param client OAuth Client 实体
     * @return 脱敏响应视图
     */
    OauthClientInfo mapToInfo(OauthClient client);
}
