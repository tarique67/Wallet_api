package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class WalletTest {

    @Test
    void expectMoneyDeposited() throws InvalidAmountException {
        Money money = mock(Money.class);
        Wallet wallet = new Wallet(1, money);
        Money moneyToAdd = new Money(100, Currency.INR);

        wallet.deposit(moneyToAdd);

        verify(money, times(1)).add(moneyToAdd);
    }

    @Test
    void expectExceptionForInvalidAmountDeposited() throws InvalidAmountException {
        Wallet wallet = new Wallet(1,new Money(100,Currency.INR));
        assertThrows(InvalidAmountException.class,()-> wallet.deposit(new Money(-50, Currency.INR)));
    }

    @Test
    void expectMoneyWithdrawn() throws InsufficientBalanceException, InvalidAmountException {
        Money money = mock(Money.class);
        Wallet wallet = new Wallet(1,money);
        Money moneyToAdd = new Money(100, Currency.INR);
        Money moneyToWithdraw = new Money(50, Currency.INR);

        wallet.deposit(moneyToAdd);
        wallet.withdraw(moneyToWithdraw);

        verify(money, times(1)).add(moneyToAdd);
        verify(money, times(1)).subtract(moneyToWithdraw);
    }

    @Test
    void expectExceptionForInsufficientBalanceWithdrawn() throws InsufficientBalanceException {
        Wallet wallet = new Wallet();
        assertThrows(InsufficientBalanceException.class, ()-> wallet.withdraw(new Money(100, Currency.INR)));
    }

    @Test
    void expectExceptionForInvalidAmountWithdrawn() throws InvalidAmountException {
        Wallet wallet = new Wallet(1,new Money(0,Currency.INR));
        assertThrows(InvalidAmountException.class,()-> wallet.withdraw(new Money(-50, Currency.INR)));
    }
}
