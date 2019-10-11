package io.simplesource.example.demo.repository.write.simplesource;

import io.simplesource.api.CommandId;
import io.simplesource.kafka.api.CommandSerdes;
import io.simplesource.kafka.model.CommandRequest;
import io.simplesource.kafka.model.CommandResponse;
import org.apache.kafka.common.serialization.Serde;

public class AvroAccountCommandSerdes implements CommandSerdes<String, AccountCommand> {

    @Override
    public Serde<CommandId> commandId() {
        return null;
    }

    @Override
    public Serde<String> aggregateKey() {
        return null;
    }

    @Override
    public Serde<CommandRequest<String, AccountCommand>> commandRequest() {
        return null;
    }

    @Override
    public Serde<CommandResponse<String>> commandResponse() {
        return null;
    }
}
