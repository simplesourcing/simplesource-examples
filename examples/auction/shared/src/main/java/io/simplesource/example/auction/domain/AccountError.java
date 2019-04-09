package io.simplesource.example.auction.domain;

import lombok.Value;

@Value
public class AccountError {
    private final Reason reason;
    private final String message;

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public static AccountError of(final Reason reason) {
        return of(reason, "");
    }
    public static AccountError of(final Reason reason, final String message) {
        return new AccountError(reason, message);
    }

    public enum Reason {
        AccountDoesNotExist,
        UserNameIsNotAvailable,
        AccountIdAlreadyExist,
        ReservationDoesNotExist,
        ReservationIdAlreadyExist,
        InvalidData,
        CommandError,
        UnknownError
    }
}
