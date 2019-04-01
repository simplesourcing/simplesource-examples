package io.simplesource.example.auction.rest.dtomappers;

import io.simplesource.example.auction.account.query.views.AccountView;
import io.simplesource.example.auction.rest.dto.AccountDetailsDto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AccountEntityToDtoMapper {
    public static AccountDetailsDto toDto(AccountView entity) {
        return new AccountDetailsDto(
            entity.getId(),
            entity.getUserName(),
            entity.getFunds(),
            entity.getAvailableFunds(),
            entity.getLastEventSequence(),
            entity.getDraftReservations()
                .stream()
                .map(AccountTransactionDtoMapper::toDto)
                .collect(Collectors.toList())
        );
    }
}
