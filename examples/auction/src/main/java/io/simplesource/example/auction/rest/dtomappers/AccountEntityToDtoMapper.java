package io.simplesource.example.auction.rest.dtomappers;

import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.rest.dto.AccountDetailsDto;
import org.mapstruct.Mapper;

public interface AccountEntityToDtoMapper {
    AccountDetailsDto toDto(AccountView entity);
}
