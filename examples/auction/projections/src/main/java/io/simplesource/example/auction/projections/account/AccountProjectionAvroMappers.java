package io.simplesource.example.auction.projections.account;

import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.*;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.generic.GenericRecord;

import java.time.Instant;
import java.util.Optional;

/**
 * TODO Why do we need to map from projection back to domain???
 */
public final class AccountProjectionAvroMappers {
    public static final GenericMapper<Optional<Reservation>, GenericRecord> ACCOUNT_TRANSACTION_PROJECTION_MAPPER = accountTransactionProjectionMapper();
    public static final GenericMapper<AccountTransactionKey, GenericRecord> ACCOUNT_TRANSACTION_KEY_MAPPER = accountTransactionKeyMapper();

    private static GenericMapper<Optional<Reservation>, GenericRecord> accountTransactionProjectionMapper() {
        return new DomainMapperBuilder(new DomainMapperRegistry())
                .optionalMapperFor(Reservation.class, io.simplesource.example.auction.account.wire.AccountTransaction.class)
                .toSerialized(r -> r.map(a -> io.simplesource.example.auction.account.wire.AccountTransaction.newBuilder()
                        .setAmount(a.amount().toString())
                        .setTimestamp(a.timestamp().toEpochMilli())
                        .setAuction(a.auction().asString())
                        .setDescription(a.description())
                        .setReservationId(a.reservationId().asString())
                        .setStatus(a.status().name())
                        .build()).orElse(null))
                .fromSerialized(a -> Optional.of(new Reservation(
                        ReservationId.of(a.getReservationId()),
                        Instant.ofEpochMilli(a.getTimestamp()),
                        AuctionKey.of(a.getAuction()),
                        Money.valueOf(a.getAmount()),
                        a.getDescription(),
                        Reservation.Status.valueOf(a.getStatus()))))
                .register()
                .build();
    }

    private static GenericMapper<AccountTransactionKey, GenericRecord> accountTransactionKeyMapper() {
        return new DomainMapperBuilder(new DomainMapperRegistry())
                .mapperFor(AccountTransactionKey.class, io.simplesource.example.auction.account.wire.AccountTransactionId.class)
                .toSerialized(a -> io.simplesource.example.auction.account.wire.AccountTransactionId.newBuilder()
                        .setReservationId(a.reservationId().asString())
                        .setAccountId(a.accountKey().asString())
                        .build())
                .fromSerialized(a -> new AccountTransactionKey(AccountKey.of(a.getAccountId()), ReservationId.of(a.getReservationId())))
                .register()
                .build();
    }
}
