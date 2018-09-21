package io.simplesource.example.auction.account.query.projection;

import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.wire.AccountTransaction;
import io.simplesource.example.auction.core.Money;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.generic.GenericRecord;

import java.util.Optional;

import static java.util.stream.Collectors.toList;

public final class AccountProjectionAvroMappers {
    public static final GenericMapper<Optional<Account>, GenericRecord> ACCOUNT_PROJECTION_MAPPER = accountProjectionMapper();
    public static final GenericMapper<Optional<Reservation>, GenericRecord> ACCOUNT_TRANSACTION_PROJECTION_MAPPER = accountTransactionProjectionMapper();
    public static final GenericMapper<AccountTransactionKey, GenericRecord> ACCOUNT_TRANSACTION_KEY_MAPPER = accountTransactionKeyMapper();

    private static GenericMapper<Optional<Account>, GenericRecord> accountProjectionMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.optionalMapperFor(Account.class, io.simplesource.example.auction.account.wire.AccountProjection.class)
                .toSerialized(r -> r.map(a -> io.simplesource.example.auction.account.wire.AccountProjection.newBuilder()
                        .setUsername(a.username())
                        .setFunds(a.funds().toString())
                        .setDraftReservations(a.fundReservations().stream().map(AccountProjectionAvroMappers::toAccountTransactionProjection).collect(toList()))
                        .build()).orElse(null))
                .fromSerialized(a -> Optional.of(new Account(a.getUsername(), Money.valueOf(a.getFunds()),
                        a.getDraftReservations().stream().map(AccountProjectionAvroMappers::fromAccountTransactionProjection).collect(toList())
                )))
                .register();
        return builder.build();
    }

    private static GenericMapper<Optional<Reservation>, GenericRecord> accountTransactionProjectionMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.optionalMapperFor(Reservation.class, io.simplesource.example.auction.account.wire.AccountTransaction.class)
                .toSerialized(r -> r.map(a -> io.simplesource.example.auction.account.wire.AccountTransaction.newBuilder()
                        .setAmount(a.amount().toString())
                        .setDescription(a.description())
                        .setReservationId(a.reservationId().asString())
                        .setStatus(a.status().name())
                        .build()).orElse(null))
                .fromSerialized(a -> Optional.of(new Reservation(ReservationId.of(a.getReservationId()), a.getDescription(), Money.valueOf(a.getAmount()),
                        Reservation.Status.valueOf(a.getStatus()))))
                .register();
        return builder.build();
    }

    private static GenericMapper<AccountTransactionKey, GenericRecord> accountTransactionKeyMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.mapperFor(AccountTransactionKey.class, io.simplesource.example.auction.account.wire.AccountTransactionId.class)
                .toSerialized(a -> io.simplesource.example.auction.account.wire.AccountTransactionId.newBuilder()
                        .setReservationId(a.reservationId().asString())
                        .setAccountId(a.accountKey().asString())
                        .build())
                .fromSerialized(a -> new AccountTransactionKey(AccountKey.of(a.getAccountId()), ReservationId.of(a.getReservationId())))
                .register();
        return builder.build();
    }

    private static AccountTransaction toAccountTransactionProjection(Reservation reservation) {
        return new AccountTransaction(reservation.reservationId().asString(), reservation.description(), reservation.amount().toString(),
                reservation.status().name());
    }

    private static Reservation fromAccountTransactionProjection(AccountTransaction transaction) {
        return new Reservation(ReservationId.of(transaction.getReservationId()), transaction.getDescription(),
                Money.valueOf(transaction.getAmount()), Reservation.Status.valueOf(transaction.getStatus()));
    }
}
