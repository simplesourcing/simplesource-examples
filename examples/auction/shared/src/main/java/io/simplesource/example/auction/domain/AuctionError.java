package io.simplesource.example.auction.domain;

import lombok.Value;

@Value
public class AuctionError {
    private final Reason reason;
    private final String message;

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public static AuctionError of(final Reason reason) {
        return of(reason, "");
    }
    public static AuctionError of(final Reason reason, final String message) {
        return new AuctionError(reason, message);
    }

    public enum Reason {
        AccountDoesNotExist,
        AuctionIdAlreadyExist,
        AuctionDoesNotExist,
        InvalidData,
        CommandError,
        UnknownError
    }
}
