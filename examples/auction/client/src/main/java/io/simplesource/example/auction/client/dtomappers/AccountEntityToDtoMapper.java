package io.simplesource.example.auction.client.dtomappers;

import io.simplesource.example.auction.client.views.AccountView;
import io.simplesource.example.auction.client.dto.AccountDetailsDto;
import org.mapstruct.Mapper;

@Mapper(uses = {MoneyMapper.class, UUIDMapper.class}, componentModel = "spring")
public interface AccountEntityToDtoMapper {
    AccountDetailsDto toDto(AccountView entity);
}
