package io.simplesource.example.auction.account.avro;

import io.simplesource.example.auction.account.domain.*;
import io.simplesource.example.auction.account.domain.AccountEvents;
import io.simplesource.example.auction.account.wire.*;
import io.simplesource.example.auction.account.wire.Account;
import io.simplesource.example.auction.core.Money;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.serialization.avro.AvroAggregateSerdes;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.generic.GenericRecord;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.simplesource.kafka.serialization.avro.AvroSpecificGenericMapper.specificDomainMapper;
import static java.util.stream.Collectors.toList;

public final class AccountAvroMappers {
    public static AggregateSerdes<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<io.simplesource.example.auction.account.domain.Account>> createDomainSerializer(String schemaRegistryUrl) {
        final AggregateSerdes<AccountKey, AccountCommand, AccountEvents.AccountEvent, Optional<io.simplesource.example.auction.account.domain.Account>> avroAggregateSerdes =
                new AvroAggregateSerdes<>(
                        aggregateMapper, buildEventMapper(), buildCommandMapper(), keyMapper,
                        schemaRegistryUrl, false,
                        io.simplesource.example.auction.account.wire.Account.SCHEMA$);
        return avroAggregateSerdes;
    }

    public static final GenericMapper<Optional<io.simplesource.example.auction.account.domain.Account>, GenericRecord> aggregateMapper = new GenericMapper<Optional<io.simplesource.example.auction.account.domain.Account>, GenericRecord>() {
        @Override
        public GenericRecord toGeneric(final Optional<io.simplesource.example.auction.account.domain.Account> maybeAccount) {
            return maybeAccount.map(account -> io.simplesource.example.auction.account.wire.Account.newBuilder()
                    .setUsername(account.username())
                    .setFunds(account.funds().getAmount())
                    .setReservations(account.fundReservations().stream().map(fromReservationDomain()).collect(toList()))
                    .build()
            ).orElse(null);
        }

        @Override
        public Optional<io.simplesource.example.auction.account.domain.Account> fromGeneric(final GenericRecord serialized) {
            if (serialized == null) return Optional.empty();

            final GenericMapper<Account, GenericRecord> mapper = specificDomainMapper();
            final io.simplesource.example.auction.account.wire.Account account = mapper.fromGeneric(serialized);
            return Optional.of(new io.simplesource.example.auction.account.domain.Account( account.getUsername(), Money.valueOf(account.getFunds()),
                    account.getReservations().stream().map(toReservationDomain()).collect(Collectors.toList())));
        }
    };

    private static Function<io.simplesource.example.auction.account.wire.Reservation, io.simplesource.example.auction.account.domain.Reservation> toReservationDomain() {
        return r -> new io.simplesource.example.auction.account.domain.Reservation(new ReservationId(r.getReservationId()), r.getDescription(),
                Money.valueOf(r.getAmount()), fromAvroReservationStatus(r.getStatus()));
    }

    private static Function<io.simplesource.example.auction.account.domain.Reservation, io.simplesource.example.auction.account.wire.Reservation> fromReservationDomain() {
        return r -> new io.simplesource.example.auction.account.wire.Reservation(r.reservationId().asString(),
                r.description(), r.amount().getAmount(), toAvroReservationStatus(r.status()));
    }

    private static String toAvroReservationStatus(io.simplesource.example.auction.account.domain.Reservation.Status status) {
        return status.name();
    }
    private static io.simplesource.example.auction.account.domain.Reservation.Status fromAvroReservationStatus(String status) {
        return io.simplesource.example.auction.account.domain.Reservation.Status.valueOf(status);
    }

    //TODO make this private method
    public static GenericMapper<AccountEvents.AccountEvent, GenericRecord> buildEventMapper() {
        DomainMapperBuilder builder = new DomainMapperBuilder(new DomainMapperRegistry());

        builder.mapperFor(AccountEvents.AccountCreated.class, AccountCreated.class)
                .toSerialized(event -> new AccountCreated(event.username(), event.initialFunds().getAmount()))
                .fromSerialized(event -> new AccountEvents.AccountCreated(event.getUsername(), Money.valueOf(event.getInitialAmount())))
                .register();

        builder.mapperFor(AccountEvents.AccountUpdated.class, AccountUpdated.class)
                .toSerialized(event -> new AccountUpdated(event.username()))
                .fromSerialized(event -> new AccountEvents.AccountUpdated(event.getUsername()))
                .register();

        builder.mapperFor(AccountEvents.FundsAdded.class, FundsAdded.class)
                .toSerialized(event -> new FundsAdded(event.addedFunds().getAmount()))
                .fromSerialized(event -> new AccountEvents.FundsAdded(Money.valueOf(event.getAmount())))
                .register();

        builder.mapperFor(AccountEvents.FundsReserved.class, FundsReserved.class)
                .toSerialized(event -> new FundsReserved(event.reservationId().asString(),  event.description(), event.amount().getAmount()))
                .fromSerialized(event -> new AccountEvents.FundsReserved(new ReservationId(event.getReservationId()),
                        event.getDescription(), Money.valueOf(event.getAmount())))
                .register();

        builder.mapperFor(AccountEvents.FundsReservationCancelled.class, ReservationCancelled.class)
                .toSerialized(event -> new ReservationCancelled(event.reservationId().asString()))
                .fromSerialized(event -> new AccountEvents.FundsReservationCancelled(new ReservationId(event.getReservationId())))
                .register();

        builder.mapperFor(AccountEvents.ReservationConfirmed.class, FundsReleased.class)
                .toSerialized(event -> new FundsReleased(event.reservationId().asString(), event.amount().getAmount()))
                .fromSerialized(event -> new AccountEvents.ReservationConfirmed(new ReservationId(event.getReservationId()),
                        Money.valueOf(event.getAmount())))
                .register();

        builder.withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Event Class not supported"));

        return builder.build();
    }

    private static GenericMapper<AccountCommand, GenericRecord> buildCommandMapper() {
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
                .toSerialized(c -> new ReserveFunds(c.reservationId().asString(), c.description(), c.funds().getAmount()))
                .fromSerialized(c -> new AccountCommand.ReserveFunds(new ReservationId((c.getReservationId())),
                        Money.valueOf(c.getAmount()), c.getDescription()))
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

    public static final GenericMapper<AccountKey, GenericRecord> keyMapper = new GenericMapper<AccountKey, GenericRecord>() {
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
