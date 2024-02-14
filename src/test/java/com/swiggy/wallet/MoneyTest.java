package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {

    @Test
    void expectMoneyCreated() {
        assertDoesNotThrow(()-> new Money(10, Currency.INR));
    }

    @Test
    void expectMoneyAdded() throws InvalidAmountException {
        Money money = new Money(0,Currency.INR);

        money.add(new Money(100, Currency.INR));

        assertEquals(new Money(100, Currency.INR), money);
    }

    @Test
    void expectMoneySubtracted() throws InvalidAmountException, InsufficientBalanceException {
        Money money = new Money(100,Currency.INR);

        money.subtract(new Money(50, Currency.INR));

        assertEquals(new Money(50, Currency.INR), money);
    }

    @Test
    void expectExceptionForInsufficientBalance() {
        Money money = new Money(10,Currency.INR);

        assertThrows(InsufficientBalanceException.class, ()-> money.subtract(new Money(50, Currency.INR)));
    }
}
