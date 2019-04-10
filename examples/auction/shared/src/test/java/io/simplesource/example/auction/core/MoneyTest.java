package io.simplesource.example.auction.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MoneyTest {


    @Test
    void shouldUpdateScaleForPassedAmount() {
        Money money = Money.valueOf(new BigDecimal(10));

        assertThat(money.getAmount()).isEqualTo(BigDecimal.valueOf(10).setScale(4));
    }

    @Test
    void addShouldReturnNewInstanceWithNewAmountAndScaleUpdated() {
        Money money1 = Money.valueOf(new BigDecimal(10));
        Money money2 = Money.valueOf(new BigDecimal(15));

        Money result = money1.add(money2);

        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(25).setScale(4));
    }

    @Test
    void shouldThrowExceptionWhenPassInvalidStringAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
           Money.valueOf("ABC");
        });
    }
}