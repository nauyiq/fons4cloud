package com.fons.cloud.auth.infrastructure.converter;

import com.fons.cloud.auth.response.AccountInfo;
import com.fons.cloud.auth.domain.entity.Account;
import com.fons.cloud.common.base.converter.CommonConverter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/9/1 14:43
 */
@SuppressWarnings("all")
@Mapper(uses = CommonConverter.class,  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountConverter {
     AccountConverter CONVERTER = Mappers.getMapper(AccountConverter.class);

    AccountInfo mapToVo(Account account);
}
