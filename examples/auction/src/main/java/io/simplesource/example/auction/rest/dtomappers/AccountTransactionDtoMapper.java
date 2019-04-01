package io.simplesource.example.auction.rest.dtomappers;

import io.simplesource.example.auction.rest.dto.AccountTransactionDto;
import io.simplesource.example.auction.account.query.views.ReservationView;

public final class AccountTransactionDtoMapper {
    public static AccountTransactionDto toDto(ReservationView reservation) {
        return new AccountTransactionDto(
            reservation.getReservationId(),
            reservation.getDescription(),
            reservation.getAmount(),
            reservation.getStatus()
        );
    }
}
