package com.swiggy.wallet;

import com.swiggy.wallet.entities.IntraWalletTransaction;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
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

import java.time.LocalDateTime;
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

    @MockBean
    private Wallet wallet;

    @MockBean
    private IntraWalletTransactionsDAO intraWalletTransactionsDAO;

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
        Wallet wallet = spy(new Wallet(1, new Money(0.0, Currency.INR)));
        User user = spy(new User(1,"testUser", "testPassword", Country.INDIA, Arrays.asList(wallet)));
        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));
        when(user.getWallets()).thenReturn(Arrays.asList(wallet));
        WalletRequestModel requestModel = new WalletRequestModel(new Money(100,Currency.INR));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100,Currency.INR), IntraWalletTransactionType.DEPOSIT, wallet, LocalDateTime.now());

        walletService.deposit(1, "testUser", requestModel);

        verify(wallet, times(1)).deposit(new Money(100,Currency.INR));
        verify(walletDao, times(1)).findById(1);
        verify(walletDao, times(1)).save(wallet);
        verify(intraWalletTransactionsDAO, times(1)).save(any());
    }

    @Test
    void expectAuthenticationFailedInDeposit() {
        when(userDao.findByUserName("nonExistentUser")).thenReturn(Optional.empty());
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        assertThrows(AuthenticationFailedException.class, () -> {
            walletService.deposit(anyInt(), "nonExistentUser", requestModel);
        });
    }

    @Test
    void expectAmountWithdrawn() throws Exception {
        Wallet wallet = new Wallet(1, new Money(0.0,Currency.INR));
        wallet.deposit(new Money(100, Currency.INR));
        User user = spy(new User(1,"testUser", "testPassword", Country.INDIA, Arrays.asList(wallet)));
        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));
        when(user.getWallets()).thenReturn(Arrays.asList(wallet));
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));
        IntraWalletTransaction withdraw = new IntraWalletTransaction(new Money(50,Currency.INR), IntraWalletTransactionType.WITHDRAW, wallet, LocalDateTime.now());

        WalletResponseModel returnedWallet = walletService.withdraw(1, "testUser", requestModel);

        assertEquals(50, returnedWallet.getMoney().getAmount());
        verify(walletDao, times(1)).findById(1);
        verify(walletDao, times(1)).save(any());

        verify(intraWalletTransactionsDAO, times(1)).save(any());
    }

    @Test
    void expectInsufficientBalanceException() throws AuthenticationFailedException, InvalidAmountException {
        Wallet wallet = new Wallet(1,new Money(0.0,Currency.INR));
        when(walletDao.findById(1)).thenReturn(Optional.of(wallet));
        User user = spy(new User("testUser", "testPassword", Country.INDIA));
        when(userDao.findByUserName("testUser")).thenReturn(Optional.of(user));
        when(user.getWallets()).thenReturn(Arrays.asList(wallet));
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));

        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.withdraw(1, "testUser", requestModel);
        });
        verify(userDao, never()).save(any());
        verify(walletDao,never()).save(any());
    }

    @Test
    void expectWalletList() {
        Wallet wallet = new Wallet();
        when(walletDao.findAll()).thenReturn(Arrays.asList(wallet));

        List<WalletResponseModel> wallets = walletService.getAllWallets();

        assertEquals(1, wallets.size());
        verify(walletDao, times(1)).findAll();
    }

    @Test
    void expectWalletListSize2() {
        Wallet firstWallet = new Wallet();
        Wallet secondWallet = new Wallet();
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
            walletService.withdraw(anyInt(), "nonExistentUser", requestModel);
        });
        verify(userDao, never()).save(any());
    }

}
