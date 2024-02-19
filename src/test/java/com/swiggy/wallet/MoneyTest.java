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
    void expectMoney100Added() throws InvalidAmountException {
        Money money = new Money(0,Currency.INR);

        money.add(new Money(100, Currency.INR));

        assertEquals(new Money(100, Currency.INR), money);
    }

    @Test
    void expectMoney50Added() throws InvalidAmountException {
        Money money = new Money(0,Currency.INR);

        money.add(new Money(50, Currency.INR));

        assertEquals(new Money(50, Currency.INR), money);
    }

    @Test
    void expectMoney100USDAdded() throws InvalidAmountException {
        Money money = new Money(0,Currency.INR);

        money.add(new Money(100, Currency.USD));

        assertEquals(new Money(8310, Currency.INR), money);
    }

    @Test
    void expectMoney100EURAdded() throws InvalidAmountException {
        Money money = new Money(0,Currency.INR);

        money.add(new Money(100, Currency.EUR));

        assertEquals(new Money(8904, Currency.INR), money);
    }

    @Test
    void expectMoney100INRSubtracted() throws InvalidAmountException, InsufficientBalanceException {
        Money money = new Money(100,Currency.INR);

        money.subtract(new Money(50, Currency.INR));

        assertEquals(new Money(50, Currency.INR), money);
    }

    @Test
    void expectMoney100USDSubtracted() throws InvalidAmountException, InsufficientBalanceException {
        Money money = new Money(0,Currency.INR);
        money.add(new Money(100, Currency.USD));

        money.subtract(new Money(100, Currency.USD));

        assertEquals(new Money(0.0, Currency.INR), money);
    }

    @Test
    void expectMoney100EURSubtracted() throws InvalidAmountException, InsufficientBalanceException {
        Money money = new Money(0,Currency.INR);
        money.add(new Money(100, Currency.EUR));

        money.subtract(new Money(100, Currency.EUR));

        assertEquals(new Money(0.0, Currency.INR), money);
    }

    @Test
    void expectExceptionForInsufficientBalance() {
        Money money = new Money(10,Currency.INR);

        assertThrows(InsufficientBalanceException.class, ()-> money.subtract(new Money(50, Currency.INR)));
    }

    @Test
    void expectExceptionAddingNegativeMoney() {
        Money money = new Money(100,Currency.INR);

        assertThrows(InvalidAmountException.class, ()-> money.subtract(new Money(-50, Currency.INR)));
    }
}
