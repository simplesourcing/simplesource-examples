package io.simplesource.example.auction.sagas;

import io.simplesource.example.auction.AppShared;
import io.simplesource.kafka.spec.WindowSpec;
import io.simplesource.saga.model.config.StreamAppConfig;
import io.simplesource.saga.model.specs.ActionSpec;
import io.simplesource.saga.model.specs.SagaSpec;
import io.simplesource.saga.saga.app.SagaApp;
import io.simplesource.saga.serialization.avro.AvroSerdes;
import io.simplesource.saga.shared.topics.TopicNamer;
import org.apache.avro.generic.GenericRecord;

import static io.simplesource.example.auction.AppShared.*;

import java.time.Duration;

/**
 * This app is responsible for the coordination and execution of sagas submitted from the client app.
 */
public class SagaCoordinatorApp {
    public static void main(String[] args) {
        runSagaCoordinator();
    }

    public static void runSagaCoordinator() {
        SagaSpec<GenericRecord> sagaSpec = SagaSpec.of(AvroSerdes.Generic.sagaSerdes(SCHEMA_REGISTRY_URL, false), new WindowSpec(3600));
        ActionSpec<GenericRecord> actionSpec = ActionSpec.of(AvroSerdes.Generic.actionSerdes(AppShared.SCHEMA_REGISTRY_URL, false));
        SagaApp<GenericRecord> sagaApp = SagaApp.of(sagaSpec, actionSpec)
                .withActions(
                        ACCOUNT_AGGREGATE_NAME,
                        AUCTION_AGGREGATE_NAME,
                        USERNAME_ALLOCATION_AGGREGATE_NAME);
        sagaApp.run(StreamAppConfig.of("saga-coordinator-1", BOOTSTRAP_SERVERS));

    }
}
