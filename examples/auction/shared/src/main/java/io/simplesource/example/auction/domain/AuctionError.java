package io.simplesource.example.auction.domain;

import io.simplesource.api.CommandError;

public abstract class AuctionError extends CommandError {
    private AuctionError() {
        super();
    }

    private AuctionError(String message) {
        super(message);
    }

    private AuctionError(Exception exception) {
        super(exception);
    }

    public static final class AccountDoesNotExist extends AuctionError {
        private static final long serialVersionUID = 8781510309716503343L;

        public AccountDoesNotExist() {
            super();
        }

        public AccountDoesNotExist(String message) {
            super(message);
        }

        public AccountDoesNotExist(Exception exception) {
            super(exception);
        }
    }

    public static final class AuctionIdAlreadyExist extends AuctionError {
        private static final long serialVersionUID = -9123068150717507165L;

        public AuctionIdAlreadyExist() {
            super();
        }

        public AuctionIdAlreadyExist(String message) {
            super(message);
        }

        public AuctionIdAlreadyExist(Exception exception) {
            super(exception);
        }
    }

    public static final class AuctionDoesNotExist extends AuctionError {
        private static final long serialVersionUID = -7841298542078486909L;

        public AuctionDoesNotExist() {
            super();
        }

        public AuctionDoesNotExist(String message) {
            super(message);
        }

        public AuctionDoesNotExist(Exception exception) {
            super(exception);
        }
    }

    public static final class InvalidData extends AuctionError {
        private static final long serialVersionUID = -298623012942130130L;

        public InvalidData() {
            super();
        }

        public InvalidData(String message) {
            super(message);
        }

        public InvalidData(Exception exception) {
            super(exception);
        }
    }

    public static class CommandError extends AuctionError {
        private static final long serialVersionUID = -7666013031545912473L;

        public CommandError() {
            super();
        }

        public CommandError(String message) {
            super(message);
        }

        public CommandError(Exception exception) {
            super(exception);
        }
    }

    public static final class UnknownError extends AuctionError {
        private static final long serialVersionUID = 4490287858261303953L;

        public UnknownError() {
            super();
        }

        public UnknownError(String message) {
            super(message);
        }

        public UnknownError(Exception exception) {
            super(exception);
        }
    }

}
