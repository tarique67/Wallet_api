package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WalletServiceTest {

    @Autowired
    private WalletService walletService;
    @MockBean
    private WalletDAO walletDao;

    @MockBean
    private Wallet wallet;

    @BeforeEach
    public void setup(){
        reset(walletDao);
    }

    @Test
    void expectAmountDepositedWithValidAmount() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50,Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        walletService.deposit(requestModel);

        verify(wallet, times(1)).deposit(any(Money.class));
        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void expectExceptionWhenDepositInvalidAmount() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(-50,Currency.INR));
        Wallet wallet = new Wallet(1,new Money(0,Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        assertThrows(InvalidAmountException.class,()-> walletService.deposit(requestModel));
    }

    @Test
    void expectAmountWithdrawn() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        walletService.withdraw(requestModel);

        verify(wallet, times(1)).withdraw(any(Money.class));
        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void expectExceptionWhenWithdrawalAmountGreaterThanBalance() throws InsufficientBalanceException {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(150, Currency.INR));
        Wallet wallet = new Wallet(1,new Money(100, Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,()-> walletService.withdraw(requestModel));
    }

    @Test
    void expectExceptionWhenWithdrawalAmountIsNegative() throws InsufficientBalanceException {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(-50, Currency.INR));
        Wallet wallet = new Wallet(1,new Money(100, Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        assertThrows(InvalidAmountException.class,()-> walletService.withdraw(requestModel));
    }
}
