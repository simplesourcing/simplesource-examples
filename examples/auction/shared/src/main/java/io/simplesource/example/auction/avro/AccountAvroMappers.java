package io.simplesource.example.auction.avro;

import io.simplesource.example.auction.account.wire.*;
import io.simplesource.example.auction.command.AccountCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.event.AccountEvent;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.domain.ReservationId;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.Conversion;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericRecord;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.simplesource.kafka.serialization.avro.AvroSpecificGenericMapper.specificDomainMapper;
import static java.util.stream.Collectors.toList;

public final class AccountAvroMappers extends AvroSerdeFactory<AccountKey, AccountCommand, AccountEvent, Optional<io.simplesource.example.auction.domain.Account>> {

    public AccountAvroMappers(String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        super(Account.SCHEMA$, schemaRegistryUrl, useMockSchemaRegistry);
    }

    @Override
    protected Collection<Conversion<?>> conversions() {
        return Stream.of(new Conversions.DecimalConversion()).collect(Collectors.toSet());
    }

    @Override
    public GenericMapper<AccountKey, GenericRecord> buildKeyMapper() {
        return new GenericMapper<AccountKey, GenericRecord>() {
            @Override
            public GenericRecord toGeneric(final AccountKey value) {
                return io.simplesource.example.auction.account.wire.AccountId.newBuilder()
                        .setId(value.asString())
                        .build();
            }

            @Override
            public AccountKey fromGeneric(final GenericRecord serialized) {
                final GenericMapper<AccountId, GenericRecord> mapper = specificDomainMapper();
                final io.simplesource.example.auction.account.wire.AccountId accountId = mapper.fromGeneric(serialized);
                return new AccountKey(accountId.getId());
            }
        };
    }

    @Override
    public GenericMapper<AccountCommand, GenericRecord> buildCommandMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.mapperFor(AccountCommand.CreateAccount.class, CreateAccount.class)
                .toSerialized(c -> new CreateAccount(c.username(), c.initialFunds().getAmount()))
                .fromSerialized(c -> new AccountCommand.CreateAccount(c.getUsername(), Money.valueOf(c.getInitialAmount())))
                .register();

        builder.mapperFor(AccountCommand.UpdateAccount.class, UpdateAccount.class)
                .toSerialized(c -> new UpdateAccount(c.username()))
                .fromSerialized(c -> new AccountCommand.UpdateAccount(c.getUsername()))
                .register();

        builder.mapperFor(AccountCommand.AddFunds.class, AddFunds.class)
                .toSerialized(c -> new AddFunds(c.funds().getAmount()))
                .fromSerialized(c -> new AccountCommand.AddFunds(Money.valueOf(c.getAmount())))
                .register();

        builder.mapperFor(AccountCommand.ReserveFunds.class, ReserveFunds.class)
                .toSerialized(c -> new ReserveFunds(c.reservationId().asString(), c.timestamp().toEpochMilli(), c.auction().asString(), c.description(), c.funds().getAmount()))
                .fromSerialized(c -> new AccountCommand.ReserveFunds(new ReservationId((c.getReservationId())),
                        Instant.ofEpochMilli(c.getTimestamp()), AuctionKey.of(c.getAuction()), Money.valueOf(c.getAmount()), c.getDescription()))
                .register();

        builder.mapperFor(AccountCommand.CancelReservation.class, CancelReservation.class)
                .toSerialized(c -> new CancelReservation(c.reservationId().asString()))
                .fromSerialized(c -> new AccountCommand.CancelReservation(new ReservationId((c.getReservationId()))))
                .register();

        builder.mapperFor(AccountCommand.ConfirmReservation.class, CommitReservation.class)
                .toSerialized(c -> new CommitReservation(c.reservationId().asString(), c.finalAmount().getAmount()))
                .fromSerialized(c -> new AccountCommand.ConfirmReservation(new ReservationId((c.getReservationId())),
                        Money.valueOf(c.getAmount())))
                .register();

        builder.withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Command Class not supported"));

        return builder.build();
    }

    @Override
    public GenericMapper<AccountEvent, GenericRecord> buildEventMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.mapperFor(AccountEvent.AccountCreated.class, AccountCreated.class)
                .toSerialized(event -> new AccountCreated(event.username(), event.initialFunds().getAmount()))
                .fromSerialized(event -> new AccountEvent.AccountCreated(event.getUsername(), Money.valueOf(event.getInitialAmount())))
                .register();

        builder.mapperFor(AccountEvent.AccountUpdated.class, AccountUpdated.class)
                .toSerialized(event -> new AccountUpdated(event.username()))
                .fromSerialized(event -> new AccountEvent.AccountUpdated(event.getUsername()))
                .register();

        builder.mapperFor(AccountEvent.FundsAdded.class, FundsAdded.class)
                .toSerialized(event -> new FundsAdded(event.addedFunds().getAmount()))
                .fromSerialized(event -> new AccountEvent.FundsAdded(Money.valueOf(event.getAmount())))
                .register();

        builder.mapperFor(AccountEvent.FundsReserved.class, FundsReserved.class)
                .toSerialized(event -> new FundsReserved(event.reservationId().asString(), event.timestamp().toEpochMilli(), event.auction().asString(),
                        event.description(), event.amount().getAmount()))
                .fromSerialized(event -> new AccountEvent.FundsReserved(new ReservationId(event.getReservationId()), Instant.ofEpochMilli(event.getTimestamp()),
                        AuctionKey.of(event.getAuction()), Money.valueOf(event.getAmount()), event.getDescription()))
                .register();

        builder.mapperFor(AccountEvent.FundsReservationCancelled.class, ReservationCancelled.class)
                .toSerialized(event -> new ReservationCancelled(event.reservationId().asString()))
                .fromSerialized(event -> new AccountEvent.FundsReservationCancelled(new ReservationId(event.getReservationId())))
                .register();

        builder.mapperFor(AccountEvent.ReservationConfirmed.class, FundsReleased.class)
                .toSerialized(event -> new FundsReleased(event.reservationId().asString(), event.amount().getAmount()))
                .fromSerialized(event -> new AccountEvent.ReservationConfirmed(new ReservationId(event.getReservationId()),
                        Money.valueOf(event.getAmount())))
                .register();

        builder.withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Event Class not supported"));

        return builder.build();
    }

    @Override
    public GenericMapper<Optional<io.simplesource.example.auction.domain.Account>, GenericRecord> buildAggregateMapper() {
        return new GenericMapper<Optional<io.simplesource.example.auction.domain.Account>, GenericRecord>() {
            @Override
            public GenericRecord toGeneric(final Optional<io.simplesource.example.auction.domain.Account> maybeAccount) {
                return maybeAccount.map(account -> io.simplesource.example.auction.account.wire.Account.newBuilder()
                        .setUsername(account.username())
                        .setFunds(account.funds().getAmount())
                        .setReservations(account.fundReservations().stream().map(fromReservationDomain()).collect(toList()))
                        .build()
                ).orElse(null);
            }

            @Override
            public Optional<io.simplesource.example.auction.domain.Account> fromGeneric(final GenericRecord serialized) {
                return Optional.ofNullable(serialized)
                        .map(s -> {
                            final GenericMapper<Account, GenericRecord> mapper = specificDomainMapper();
                            final io.simplesource.example.auction.account.wire.Account account = mapper.fromGeneric(s);
                            return new io.simplesource.example.auction.domain.Account(
                                    account.getUsername(),
                                    Money.valueOf(account.getFunds()),
                                    account.getReservations().stream().map(toReservationDomain()).collect(Collectors.toList()));
                        });
            }
        };
    }

    private static Function<io.simplesource.example.auction.account.wire.Reservation, io.simplesource.example.auction.domain.Reservation> toReservationDomain() {
        return r -> new io.simplesource.example.auction.domain.Reservation(ReservationId.of(r.getReservationId()), Instant.ofEpochMilli(r.getTimestamp()),
                AuctionKey.of(r.getAuction()), Money.valueOf(r.getAmount()),r.getDescription(), fromAvroReservationStatus(r.getStatus()));
    }

    private static Function<io.simplesource.example.auction.domain.Reservation, io.simplesource.example.auction.account.wire.Reservation> fromReservationDomain() {
        return r -> new io.simplesource.example.auction.account.wire.Reservation(r.reservationId().asString(), r.timestamp().toEpochMilli(),
                 r.auction().asString(), r.description(), r.amount().getAmount(), toAvroReservationStatus(r.status()));
    }

    private static String toAvroReservationStatus(io.simplesource.example.auction.domain.Reservation.Status status) {
        return status.name();
    }

    private static io.simplesource.example.auction.domain.Reservation.Status fromAvroReservationStatus(String status) {
        return io.simplesource.example.auction.domain.Reservation.Status.valueOf(status);
    }
}