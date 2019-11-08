package io.simplesource.example.auction.client.service;

import io.simplesource.api.CommandAPI;
import io.simplesource.data.FutureResult;
import io.simplesource.data.NonEmptyList;
import io.simplesource.data.Sequence;
import io.simplesource.example.auction.auction.wire.AuctionStatus;
import io.simplesource.example.auction.client.repository.AccountRepository;
import io.simplesource.example.auction.client.repository.AuctionRepository;
import io.simplesource.example.auction.client.views.AuctionView;
import io.simplesource.example.auction.command.AuctionCommand;
import io.simplesource.example.auction.core.Money;
import io.simplesource.example.auction.domain.Auction;
import io.simplesource.example.auction.domain.AuctionError;
import io.simplesource.example.auction.domain.AuctionKey;
import io.simplesource.saga.model.api.SagaAPI;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionWriteServiceImplTest {

    @Mock
    private CommandAPI<AuctionKey, AuctionCommand> commandApi;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private SagaAPI<GenericRecord> sagaApi;

    private AuctionWriteService auctionWriteService;

    private Auction auction = new Auction("Bob", "Crayon", "Brand new red crayon",
            Money.ZERO, null, Duration.ofDays(1), AuctionStatus.CREATED, null, null, Collections.emptyList());
    private AuctionKey key = new AuctionKey(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        auctionWriteService = new AuctionWriteServiceImpl(commandApi, sagaApi, accountRepository, auctionRepository);
    }

    @Test
    void createShouldPublishAndQueryCommand() {
        when(auctionRepository.findById(key.id().toString())).thenReturn(Optional.empty());
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AuctionError, Sequence> result = auctionWriteService.createAuction(key, auction);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }

    @Test
    void createShouldReturnErrorWhenAuctionWithSameKeyExists() {
        AuctionView auctionView = new AuctionView();
        auctionView.setId(key.id().toString());
        when(auctionRepository.findById(key.id().toString())).thenReturn(Optional.of(auctionView));

        FutureResult<AuctionError, Sequence> result = auctionWriteService.createAuction(key, auction);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(new AuctionError.AuctionIdAlreadyExist(String.format("Auction ID %s already exist", key.asString()))));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void createShouldReturnErrorForMissingFields() {
        when(auctionRepository.findById(key.id().toString())).thenReturn(Optional.empty());

        Auction auction = new Auction("", "", "",
                Money.ZERO, null, Duration.ofDays(1), AuctionStatus.CREATED, null, null, Collections.emptyList());
        FutureResult<AuctionError, Sequence> result = auctionWriteService.createAuction(key, auction);
        assertThat(result.future().join().isFailure()).isTrue();
        assertThat(result.future().join().failureReasons()).contains(
                NonEmptyList.of(
                        new AuctionError.InvalidData("Creator can not be empty"),
                        new AuctionError.InvalidData("Title can not be empty"),
                        new AuctionError.InvalidData("Description can not be empty")
                ));
        verifyZeroInteractions(commandApi);
    }

    @Test
    void startAuctionShouldPublishAndQueryCommand() {
        AuctionView auctionView = new AuctionView();
        auctionView.setId(key.id().toString());
        when(auctionRepository.findById(key.id().toString())).thenReturn(Optional.of(auctionView));
        when(commandApi.publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class)))
                .thenReturn(FutureResult.of(Sequence.first()));

        FutureResult<AuctionError, Sequence> result = auctionWriteService.startAuction(key);
        assertThat(result.future().join().isSuccess()).isTrue();
        verify(commandApi).publishAndQueryCommand(any(CommandAPI.Request.class), any(Duration.class));
    }
}
