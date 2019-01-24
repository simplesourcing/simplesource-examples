package io.simplesource.example.auction.rest.dto;

public final class FieldErrorDto {
    private final String field;
    private final String message;

    public FieldErrorDto(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
