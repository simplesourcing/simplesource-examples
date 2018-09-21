package io.simplesource.example.auction.rest.dto;

import java.util.ArrayList;
import java.util.List;

public final class ValidationErrorDto {
    private List<FieldErrorDto> fieldErrors = new ArrayList<>();

    public ValidationErrorDto() {

    }

    public void addFieldError(String path, String message) {
        FieldErrorDto error = new FieldErrorDto(path, message);
        fieldErrors.add(error);
    }
}
