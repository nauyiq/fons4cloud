package com.fons.cloud.admin.infrastructure.converter;

import com.fons.cloud.admin.api.response.AdminTokenResponse;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.auth.response.TokenInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * admin 认证响应转换器。
 */
@Mapper
public interface AdminAuthConverter {

    AdminAuthConverter CONVERTER = Mappers.getMapper(AdminAuthConverter.class);

    /**
     * 将认证服务 Token 和 admin 用户绑定信息合并为 admin 对外响应。
     *
     * @param tokenInfo 认证服务 Token
     * @param adminUser admin 用户绑定
     * @return admin Token 响应
     */
    @Mapping(target = "userId", source = "adminUser.id")
    @Mapping(target = "username", source = "adminUser.username")
    AdminTokenResponse mapToAdminTokenResponse(TokenInfo tokenInfo, AdminUser adminUser);
}
