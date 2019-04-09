package io.simplesource.example.auction.avro;

import io.simplesource.example.auction.auction.wire.*;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.AccountKey;
import io.simplesource.example.auction.event.AuctionEvent;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.example.auction.domain.ReservationId;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperBuilder;
import io.simplesource.kafka.serialization.avro.mappers.DomainMapperRegistry;
import io.simplesource.kafka.serialization.util.GenericMapper;
import org.apache.avro.Conversion;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.simplesource.kafka.serialization.avro.AvroSpecificGenericMapper.specificDomainMapper;

public final class AuctionAvroMappers extends AvroSerdeFactory<AuctionKey, AuctionCommand, AuctionEvent, Optional<io.simplesource.example.auction.domain.Auction>> {

    public AuctionAvroMappers(String schemaRegistryUrl, boolean useMockSchemaRegistry) {
        super(Auction.SCHEMA$, schemaRegistryUrl, useMockSchemaRegistry);
    }

    @Override
    protected Collection<Conversion<?>> conversions() {
        return Stream.of(new Conversions.DecimalConversion()).collect(Collectors.toSet());
    }

    @Override
    public GenericMapper<AuctionKey, GenericRecord> buildKeyMapper() {
        return new GenericMapper<AuctionKey, GenericRecord>() {
            @Override
            public GenericRecord toGeneric(final AuctionKey value) {
                return AuctionId.newBuilder()
                        .setId(value.asString())
                        .build();
            }

            @Override
            public AuctionKey fromGeneric(final GenericRecord serialized) {
                final GenericMapper<AuctionId, GenericRecord> mapper = specificDomainMapper();
                final AuctionId auctionId = mapper.fromGeneric(serialized);
                return new AuctionKey(auctionId.getId());
            }
        };
    }

    @Override
    public GenericMapper<AuctionCommand, GenericRecord> buildCommandMapper() {
        return new DomainMapperBuilder(new DomainMapperRegistry())

                .mapperFor(AuctionCommand.CreateAuction.class, CreateAuction.class)
                .toSerialized(c -> new CreateAuction(
                        c.creator(),
                        c.title(),
                        c.description(),
                        c.reservePrice().getAmount(),
                        c.duration().toMillis()))
                .fromSerialized(c -> new AuctionCommand.CreateAuction(
                        c.getCreator(),
                        c.getTitle(),
                        c.getDescription(),
                        Money.valueOf(c.getReservePrice()),
                        Duration.ofMillis(c.getDuration())))
                .register()

                .mapperFor(AuctionCommand.StartAuction.class, StartAuction.class)
                .toSerialized(c -> new StartAuction(
                        c.start().toEpochMilli()
                ))
                .fromSerialized(c -> new AuctionCommand.StartAuction(
                        Instant.ofEpochMilli(c.getStart())
                ))
                .register()

                .mapperFor(AuctionCommand.PlaceBid.class, PlaceBid.class)
                .toSerialized(c -> new PlaceBid(
                    c.reservationId().asString(),
                        c.timestamp().toEpochMilli(),
                        c.bidder().asString(),
                        c.amount().getAmount()
                ))
                .fromSerialized(c -> new AuctionCommand.PlaceBid(
                        ReservationId.of(c.getReservationId()),
                        Instant.ofEpochMilli(c.getTimestamp()),
                        AccountKey.of(c.getBidder()),
                        Money.valueOf(c.getAmount())
                ))
                .register()

                .mapperFor(AuctionCommand.CompleteAuction.class, CompleteAuction.class)
                .toSerialized(c -> new CompleteAuction(
                ))
                .fromSerialized(c -> new AuctionCommand.CompleteAuction(
                ))
                .register()

                .withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Command Class not supported"))
                .build();
    }

    @Override
    public GenericMapper<AuctionEvent, GenericRecord> buildEventMapper() {
        return new DomainMapperBuilder()
                .mapperFor(AuctionEvent.AuctionCreated.class, AuctionCreated.class)
                .toSerialized(event -> new AuctionCreated(
                        event.creator(),
                        event.title(),
                        event.description(),
                        event.reservePrice().getAmount(),
                        event.duration().toMillis()))
                .fromSerialized(event -> new AuctionEvent.AuctionCreated(
                        event.getCreator(),
                        event.getTitle(),
                        event.getDescription(),
                        Money.valueOf(event.getReservePrice()),
                        Duration.ofMillis(event.getDuration())))
                .register()

                .mapperFor(AuctionEvent.AuctionStarted.class, AuctionStarted.class)
                .toSerialized(event -> new AuctionStarted(
                        event.started().toEpochMilli()
                ))
                .fromSerialized(event -> new AuctionEvent.AuctionStarted(
                        Instant.ofEpochMilli(event.getStart())
                ))
                .register()

                .mapperFor(AuctionEvent.BidPlaced.class, BidPlaced.class)
                .toSerialized(event -> new BidPlaced(
                        event.reservationId().asString(),
                        event.timestamp().toEpochMilli(),
                        event.bidder().asString(),
                        event.amount().getAmount()
                ))
                .fromSerialized(event -> new AuctionEvent.BidPlaced(
                        ReservationId.of(event.getReservationId()),
                        Instant.ofEpochMilli(event.getTimestamp()),
                        AccountKey.of(event.getBidder()),
                        Money.valueOf(event.getAmount())
                ))
                .register()

                .mapperFor(AuctionEvent.AuctionCompleted.class, AuctionCompleted.class)
                .toSerialized(event -> new AuctionCompleted(
                ))
                .fromSerialized(event -> new AuctionEvent.AuctionCompleted(
                ))
                .register()

                .withExceptionSupplierForNotRegisteredMapper(() -> new IllegalArgumentException("Event Class not supported"))
                .build();
    }

    @Override
    public GenericMapper<Optional<io.simplesource.example.auction.domain.Auction>, GenericRecord> buildAggregateMapper() {
        return new GenericMapper<Optional<io.simplesource.example.auction.domain.Auction>, GenericRecord>() {
            @Override
            public GenericRecord toGeneric(final Optional<io.simplesource.example.auction.domain.Auction> maybeAuction) {
                return maybeAuction.map(auction -> io.simplesource.example.auction.auction.wire.Auction.newBuilder()
                        .setCreator(auction.creator())
                        .setTitle(auction.title())
                        .setDescription(auction.description())
                        .setReservePrice(auction.reservePrice().getAmount())
                        .setPrice(Optional.ofNullable(auction.price()).map(Money::getAmount).orElse(null))
                        .setDuration(auction.duration().toMillis())
                        .setStatus(auction.status().name())
                        .setStart(Optional.ofNullable(auction.start()).map(Instant::toEpochMilli).orElse(null))
                        .setWinner(Optional.ofNullable(auction.winner()).map(AccountKey::asString).orElse(null))
                        .setBids(auction.bids().stream().map(b -> new Bid(
                                b.reservationId().asString(),
                                b.timestamp().toEpochMilli(),
                                b.bidder().asString(),
                                b.amount().getAmount())).collect(Collectors.toList())
                        )
                        .build()
                ).orElse(null);
            }

            @Override
            public Optional<io.simplesource.example.auction.domain.Auction> fromGeneric(final GenericRecord serialized) {
                if (serialized == null) return Optional.empty();

                final GenericMapper<Auction, GenericRecord> mapper = specificDomainMapper();
                final Auction auction = mapper.fromGeneric(serialized);
                return Optional.of(new io.simplesource.example.auction.domain.Auction(
                        auction.getCreator(),
                        auction.getTitle(),
                        auction.getDescription(),
                        Money.valueOf(auction.getReservePrice()),
                        Optional.ofNullable(auction.getPrice()).map(Money::valueOf).orElse(null),
                        Duration.ofMillis(auction.getDuration()),
                        AuctionStatus.valueOf(auction.getStatus()),
                        Optional.ofNullable(auction.getStart()).map(Instant::ofEpochMilli).orElse(null),
                        Optional.ofNullable(auction.getWinner()).map(AccountKey::of).orElse(null),
                        auction.getBids().stream().map(b -> new io.simplesource.example.auction.domain.Bid(
                                ReservationId.of(b.getReservationId()),
                                Instant.ofEpochMilli(b.getTimestamp()),
                                AccountKey.of(b.getBidder()),
                                Money.valueOf(b.getAmount()))).collect(Collectors.toList())
                ));
            }
        };
    }
}
