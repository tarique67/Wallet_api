package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.services.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
public class WalletServiceTest {

    @MockBean
    private WalletDAO walletDao;

    @MockBean
    private UserDAO userDao;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    public void setup(){
        openMocks(this);
    }

    @Test
    void expectWalletCreated() {
        Wallet wallet = new Wallet();
        when(walletDao.save(any())).thenReturn(wallet);

        Wallet createdWallet = walletService.create(new Wallet());

        assertNotNull(createdWallet);
        verify(walletDao, times(1)).save(any());
    }

    @Test
    void expectAmountDepositedWithValidAmount() throws Exception {
        User user = new User();
        user.setUserName("testUser");
        user.setWallet(new Wallet());
        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);
        WalletRequestModel requestModel = new WalletRequestModel(new Money(100,Currency.INR));

        walletService.deposit("testUser", requestModel);

        verify(userDao, times(1)).findByUserName("testUser");
        verify(userDao, times(1)).save(any());
    }

    @Test
    void expectAuthenticationFailedInDeposit() {
        when(userDao.findByUserName("nonExistentUser")).thenReturn(Optional.empty());
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        assertThrows(AuthenticationFailedException.class, () -> {
            walletService.deposit("nonExistentUser", requestModel);
        });
    }

    @Test
    void expectAmountWithdrawn() throws Exception {
        Wallet wallet = new Wallet();
        wallet.deposit(new Money(100, Currency.INR));
        User user = new User("testUser", "testPassword", wallet);

        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        WalletResponseModel returnedWallet = walletService.withdraw("testUser", requestModel);

        assertEquals(50, wallet.getMoney().getAmount());
        verify(userDao, times(1)).findByUserName("testUser");
        verify(userDao, times(1)).save(any());
    }

    @Test
    void expectInsufficientBalanceException() throws AuthenticationFailedException, InvalidAmountException {
        User user = new User("testUser", "testPassword", new Wallet());
        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.withdraw("testUser", requestModel);
        });
        verify(userDao, never()).save(any());
        verify(walletDao,never()).save(any());
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

    @Test
    void expectAuthenticationFailed() {
        when(userDao.findByUserName("nonExistentUser")).thenReturn(Optional.empty());
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        assertThrows(AuthenticationFailedException.class, () -> {
            walletService.withdraw("nonExistentUser", requestModel);
        });
        verify(userDao, never()).save(any());
    }
}
