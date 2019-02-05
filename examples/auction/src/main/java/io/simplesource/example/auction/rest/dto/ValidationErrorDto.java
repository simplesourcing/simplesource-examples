package io.simplesource.example.auction.rest.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ValidationErrorDto {
    private final List<FieldErrorDto> fieldErrors;

    public ValidationErrorDto(List<FieldErrorDto> fieldErrors) {
        this.fieldErrors = Collections.unmodifiableList(fieldErrors);
    }

}
