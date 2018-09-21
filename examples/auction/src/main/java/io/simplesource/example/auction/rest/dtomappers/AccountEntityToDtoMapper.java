package io.simplesource.example.auction.rest.dtomappers;

import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.rest.dto.AccountDetailsDto;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, UUIDMapper.class}, componentModel = "spring")
public interface AccountEntityToDtoMapper {
    AccountDetailsDto toDto(AccountView entity);
}
