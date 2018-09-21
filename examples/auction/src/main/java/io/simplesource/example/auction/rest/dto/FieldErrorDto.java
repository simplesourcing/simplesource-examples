package io.simplesource.example.auction.rest.dto;

public final class FieldErrorDto {
    private String field;
    private String message;

    public FieldErrorDto(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
