package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.entities.WalletResponseModel;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void expectWalletCreated() {
        walletService.create(wallet);

        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void expectAmountDepositedWithValidAmount() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50,Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        walletService.deposit(1, requestModel);

        verify(wallet, times(1)).deposit(any(Money.class));
        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void expectAmountWithdrawn() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));

        walletService.withdraw(1, requestModel);

        verify(wallet, times(1)).withdraw(any(Money.class));
        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void expectWalletList() {
        Wallet wallet = new Wallet(1, new Money(0, Currency.INR));
        when(walletDao.findAll()).thenReturn(Arrays.asList(wallet));

        List<WalletResponseModel> wallets = walletService.getAllWallets();

        assertEquals(1, wallets.size());
        verify(walletDao, times(1)).findAll();
    }

    @Test
    void expectWalletListSize2() {
        Wallet firstWallet = new Wallet(1, new Money(0, Currency.INR));
        Wallet secondWallet = new Wallet(2, new Money(0, Currency.INR));
        when(walletDao.findAll()).thenReturn(Arrays.asList(firstWallet,secondWallet));

        List<WalletResponseModel> wallets = walletService.getAllWallets();

        assertEquals(2, wallets.size());
        verify(walletDao, times(1)).findAll();
    }

}
