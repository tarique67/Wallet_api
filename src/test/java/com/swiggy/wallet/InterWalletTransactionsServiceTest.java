package com.swiggy.wallet;

import com.swiggy.wallet.entities.*;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.InterWalletTransactionDAO;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.services.InterWalletTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
public class InterWalletTransactionsServiceTest {

    @Mock
    private UserDAO userDao;

    @Mock
    private InterWalletTransactionDAO interWalletTransactionDao;

    @Mock
    private IntraWalletTransactionsDAO intraWalletTransactionsDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletDAO walletDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InterWalletTransactionServiceImpl interWalletTransactionService;

    @BeforeEach
    void setUp(){
        openMocks(this);
    }

    @Test
    void expectTransactionSuccessful() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        Wallet receiverWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        senderWallet.deposit(new Money(100.0,Currency.INR));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, Arrays.asList(receiverWallet));
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        InterWalletTransaction transaction = new InterWalletTransaction(1, sender, 1, receiver, 2, new Money(0.0, Currency.INR), deposit, withdrawal);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));
        when(interWalletTransactionDao.save(any())).thenReturn(transaction);

        interWalletTransactionService.transact(requestModel);

        verify(senderWallet, times(1)).deposit(requestModel.getMoney());
        verify(senderWallet, times(1)).withdraw(requestModel.getMoney());
        verify(receiverWallet, times(1)).deposit(requestModel.getMoney());
        verify(userDao, times(1)).save(sender);
        verify(userDao, times(1)).save(receiver);
        verify(interWalletTransactionDao, times(1)).save(any());
    }

    @Test
    void expectServiceChargeDeductedOnTransaction() throws InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException, InsufficientBalanceException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        Wallet receiverWallet = spy(new Wallet(2, new Money(0, Currency.USD)));
        senderWallet.deposit(new Money(100.0,Currency.INR));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, Arrays.asList(receiverWallet));
        Money moneyToTransact = new Money(100.0, Currency.INR);
        Money moneyAfterServiceChargeCut = new Money(90.0, Currency.INR);
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"receiver", 2, moneyToTransact));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        InterWalletTransaction transaction = new InterWalletTransaction(1, sender, 1, receiver, 2, new Money(0.0, Currency.INR), deposit, withdrawal);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));
        when(interWalletTransactionDao.save(any())).thenReturn(transaction);

        interWalletTransactionService.transact(requestModel);

        verify(senderWallet, times(1)).deposit(new Money(100.0,Currency.INR));
        verify(senderWallet, times(1)).withdraw(moneyToTransact);
        verify(receiverWallet, times(1)).deposit(moneyAfterServiceChargeCut);
        verify(userDao, times(1)).save(sender);
        verify(userDao, times(1)).save(receiver);
    }

    @Test
    void expectExceptionWhenTransferAmountLessThanServiceCharge() throws InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException, InsufficientBalanceException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        Wallet receiverWallet = spy(new Wallet(2, new Money(0, Currency.USD)));
        senderWallet.deposit(new Money(100.0,Currency.INR));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, Arrays.asList(receiverWallet));
        Money moneyToTransact = new Money(9.0, Currency.INR);
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"receiver", 2, moneyToTransact));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        InterWalletTransaction transaction = new InterWalletTransaction(1, sender, 1, receiver, 2, new Money(0.0, Currency.INR), deposit, withdrawal);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));
        when(interWalletTransactionDao.save(any())).thenReturn(transaction);

        assertThrows(InvalidAmountException.class, ()-> interWalletTransactionService.transact(requestModel));
        verify(senderWallet, never()).withdraw(any());
        verify(receiverWallet, never()).deposit(any());
        verify(userDao, never()).save(sender);
        verify(userDao, never()).save(receiver);
    }

    @Test
    void expectReceiverNotFoundOnTransaction() throws InsufficientBalanceException, InvalidAmountException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        InterWalletTransactionRequestModel requestModel = new InterWalletTransactionRequestModel(1, "receiver", 2,new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()-> interWalletTransactionService.transact(requestModel));

        verify(senderWallet, times(0)).withdraw(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
    }

    @Test
    void expectSameWalletsExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        InterWalletTransactionRequestModel requestModel = new InterWalletTransactionRequestModel(1, "sender", 1,new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));

        assertThrows(SameWalletsForTransactionException.class,()-> interWalletTransactionService.transact(requestModel));

        verify(senderWallet, times(0)).deposit(requestModel.getMoney());
        verify(senderWallet, times(0)).withdraw(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
    }

    @Test
    void expectSenderWalletNotFoundExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet receiverWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, new ArrayList<>());
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, Arrays.asList(receiverWallet));
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));

        assertThrows(WalletNotFoundException.class,()-> interWalletTransactionService.transact(requestModel));
        verify(receiverWallet, times(0)).deposit(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
        verify(userDao, times(0)).save(receiver);
    }

    @Test
    void expectReceiverWalletNotFoundExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet senderWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, new ArrayList<>());
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(2)).thenReturn(Optional.of(senderWallet));

        assertThrows(WalletNotFoundException.class,()-> interWalletTransactionService.transact(requestModel));
        verify(senderWallet, times(0)).deposit(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
        verify(userDao, times(0)).save(receiver);
    }

    @Test
    void expectTransactionSuccessfulForTwoDifferentWallets() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet firstSenderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        Wallet secondSenderWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        firstSenderWallet.deposit(new Money(100.0,Currency.INR));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(firstSenderWallet, secondSenderWallet));
        InterWalletTransactionRequestModel requestModel = spy(new InterWalletTransactionRequestModel(1,"sender", 2, new Money(90.0, Currency.INR)));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,secondSenderWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,firstSenderWallet, LocalDateTime.now());
        InterWalletTransaction transaction = new InterWalletTransaction(1, sender, 1, sender, 2, new Money(0.0, Currency.INR), deposit, withdrawal);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(walletDao.findById(1)).thenReturn(Optional.of(firstSenderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(secondSenderWallet));
        when(interWalletTransactionDao.save(any())).thenReturn(transaction);

        interWalletTransactionService.transact(requestModel);

        verify(firstSenderWallet, times(1)).deposit(new Money(100.0,Currency.INR));
        verify(firstSenderWallet, times(1)).withdraw(requestModel.getMoney());
        verify(secondSenderWallet, times(1)).deposit(requestModel.getMoney());
        verify(userDao, times(2)).save(sender);
    }

}
