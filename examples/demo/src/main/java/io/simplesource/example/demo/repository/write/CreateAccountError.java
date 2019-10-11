package io.simplesource.example.demo.repository.write;

import java.util.Optional;
import java.util.function.Function;

public abstract class CreateAccountError {
    private CreateAccountError(){}

    public abstract String message();

    public abstract <T> T match(Function<AccountAlreadyExists, T> f1, Function<AccountNameNotSet, T> f2, Function<OpeningBalanceLessThanZero, T> f3);

    public static final AccountAlreadyExists ACCOUNT_ALREADY_EXISTS = new AccountAlreadyExists();
    public static final AccountNameNotSet ACCOUNT_NAME_NOT_SET = new AccountNameNotSet();
    public static final OpeningBalanceLessThanZero OPENING_BALANCE_LESS_THAN_ZERO = new OpeningBalanceLessThanZero();

    public static final class AccountAlreadyExists extends CreateAccountError {
        private AccountAlreadyExists() {}

        @Override
        public String message() {
            return "Account already exists";
        }

        @Override
        public <T> T match(Function<AccountAlreadyExists, T> f1, Function<AccountNameNotSet, T> f2, Function<OpeningBalanceLessThanZero, T> f3) {
            return f1.apply(this);
        }
    }

    public static final class AccountNameNotSet extends CreateAccountError {
        private AccountNameNotSet() {}

        @Override
        public String message() {
            return "Account name is not set";
        }

        @Override
        public <T> T match(Function<AccountAlreadyExists, T> f1, Function<AccountNameNotSet, T> f2, Function<OpeningBalanceLessThanZero, T> f3) {
            return f2.apply(this);
        }
    }

    public static final class OpeningBalanceLessThanZero extends CreateAccountError {
        private OpeningBalanceLessThanZero() {}

        @Override
        public String message() {
            return "Opening balance cannot be negative";
        }

        @Override
        public <T> T match(Function<AccountAlreadyExists, T> f1, Function<AccountNameNotSet, T> f2, Function<OpeningBalanceLessThanZero, T> f3) {
            return f3.apply(this);
        }
    }

    public static Optional<CreateAccountError> fromString(String s) {
        if(ACCOUNT_ALREADY_EXISTS.message().equals(s)) {
            return Optional.of(ACCOUNT_ALREADY_EXISTS);
        } else if (ACCOUNT_NAME_NOT_SET.message().equals(s)) {
            return Optional.of(ACCOUNT_NAME_NOT_SET);
        } else if (OPENING_BALANCE_LESS_THAN_ZERO.message().equals(s)) {
            return Optional.of(OPENING_BALANCE_LESS_THAN_ZERO);
        } else {
            return Optional.empty();
        }
    }


}
