package io.simplesource.example.auction.domain;

import io.simplesource.api.CommandError;

public abstract class AccountError extends CommandError {

    private AccountError() {
        super();
    }

    private AccountError(String message) {
        super(message);
    }

    private AccountError(Exception e) {
        super(e);
    }

    public static final class AccountDoesNotExist extends AccountError {
        private static final long serialVersionUID = -215585474456449068L;

        public AccountDoesNotExist() {
            super();
        }

        public AccountDoesNotExist(String message) {
            super(message);
        }

        public AccountDoesNotExist(Exception e) {
            super(e);
        }
    }

    public static final class UserNameIsNotAvailable extends AccountError {
        private static final long serialVersionUID = 7884359587950289618L;

        public UserNameIsNotAvailable() {
            super();
        }

        public UserNameIsNotAvailable(String message) {
            super(message);
        }

        public UserNameIsNotAvailable(Exception e) {
            super(e);
        }
    }

    public static final class AccountIdAlreadyExist extends AccountError {
        private static final long serialVersionUID = -3309739681235200338L;

        public AccountIdAlreadyExist() {
            super();
        }

        public AccountIdAlreadyExist(String message) {
            super(message);
        }

        public AccountIdAlreadyExist(Exception e) {
            super(e);
        }
    }

    public static final class ReservationDoesNotExist extends AccountError {
        private static final long serialVersionUID = -2113054020515863952L;

        public ReservationDoesNotExist() {
            super();
        }

        public ReservationDoesNotExist(String message) {
            super(message);
        }

        public ReservationDoesNotExist(Exception e) {
            super(e);
        }
    }

    public static final class ReservationIdAlreadyExist extends AccountError {
        private static final long serialVersionUID = 2033262596717296449L;

        public ReservationIdAlreadyExist() {
            super();
        }

        public ReservationIdAlreadyExist(String message) {
            super(message);
        }

        public ReservationIdAlreadyExist(Exception e) {
            super(e);
        }
    }

    public static final class InvalidData extends AccountError {
        private static final long serialVersionUID = 1688373418492304969L;

        public InvalidData() {
            super();
        }

        public InvalidData(String message) {
            super(message);
        }

        public InvalidData(Exception e) {
            super(e);
        }
    }

    public static final class CommandError extends AccountError {
        private static final long serialVersionUID = -1415934367096806312L;

        public CommandError() {
            super();
        }

        public CommandError(String message) {
            super(message);
        }

        public CommandError(Exception e) {
            super(e);
        }
    }

    public static final class UnknownError extends AccountError {
        private static final long serialVersionUID = -4978397213848019725L;

        public UnknownError() {
            super();
        }

        public UnknownError(String message) {
            super(message);
        }

        public UnknownError(Exception e) {
            super(e);
        }
    }

}
