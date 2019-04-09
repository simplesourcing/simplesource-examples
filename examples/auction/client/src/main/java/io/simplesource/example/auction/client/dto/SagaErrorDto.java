package io.simplesource.example.auction.client.dto;

import io.simplesource.saga.model.saga.SagaError;

import java.util.List;
import java.util.UUID;

public final class SagaErrorDto {
    private UUID sagaId;
    private List<SagaError> errors;

    public SagaErrorDto(UUID sagaId, List<SagaError> errors) {
        this.sagaId = sagaId;
        this.errors = errors;
    }

    public UUID getSagaId() {
        return sagaId;
    }

    public void setSagaId(UUID sagaId) {
        this.sagaId = sagaId;
    }

    public List<SagaError> getErrors() {
        return errors;
    }

    public void setErrors(List<SagaError> errors) {
        this.errors = errors;
    }
}
