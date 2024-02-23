package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WalletTest {

    @Mock
    private Money money;

    @Test
    void expectMoneyDeposited() throws InvalidAmountException {
        Wallet wallet = new Wallet(1,money);
        Money moneyToAdd = new Money(100, Currency.INR);

        wallet.deposit(moneyToAdd);

        verify(money, times(1)).add(moneyToAdd);
    }

    @Test
    void expectExceptionForInvalidAmountDeposited() throws InvalidAmountException {
        Wallet wallet = new Wallet(Country.INDIA);
        assertThrows(InvalidAmountException.class,()-> wallet.deposit(new Money(-50, Currency.INR)));
    }

    @Test
    void expectMoneyWithdrawn() throws InsufficientBalanceException, InvalidAmountException {
        Wallet wallet = new Wallet(1, money);
        Money moneyToAdd = new Money(100, Currency.INR);
        Money moneyToWithdraw = new Money(50, Currency.INR);

        wallet.deposit(moneyToAdd);
        wallet.withdraw(moneyToWithdraw);

        verify(money, times(1)).add(moneyToAdd);
        verify(money, times(1)).subtract(moneyToWithdraw);
    }

    @Test
    void expectExceptionForInsufficientBalanceWhenWithdrawing() throws InsufficientBalanceException, InvalidAmountException {
        Wallet wallet = new Wallet(Country.INDIA);
        assertThrows(InsufficientBalanceException.class, ()-> wallet.withdraw(new Money(100, Currency.INR)));
    }

    @Test
    void expectExceptionForInvalidAmountWithdrawn() throws InvalidAmountException {
        Wallet wallet = new Wallet(Country.INDIA);
        assertThrows(InvalidAmountException.class,()-> wallet.withdraw(new Money(-50, Currency.INR)));
    }

    @Test
    void expectCurrencyINRForIndia() {
        Wallet wallet = new Wallet(Country.INDIA);

        assertEquals(new Money(0.0,Currency.INR), wallet.getMoney());
    }

    @Test
    void expectCurrencyUSDForUSA() {
        Wallet wallet = new Wallet(Country.USA);

        assertEquals(new Money(0.0,Currency.USD), wallet.getMoney());
    }
}
