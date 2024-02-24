package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.TransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionServicesImpl;
import com.swiggy.wallet.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.swiggy.wallet.constants.Constants.SERVICE_CHARGE_IN_INR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
public class TransactionServiceTest {

    @Mock
    private UserDAO userDao;

    @Mock
    private TransactionDAO transactionDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletDAO walletDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionServicesImpl transactionService;

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
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));

        transactionService.transact(requestModel);

        verify(senderWallet, times(1)).deposit(requestModel.getMoney());
        verify(senderWallet, times(1)).withdraw(requestModel.getMoney());
        verify(receiverWallet, times(1)).deposit(requestModel.getMoney());
        verify(userDao, times(1)).save(sender);
        verify(userDao, times(1)).save(receiver);
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
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"receiver", 2, moneyToTransact));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));

        transactionService.transact(requestModel);

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
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"receiver", 2, moneyToTransact));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));

        assertThrows(InvalidAmountException.class, ()-> transactionService.transact(requestModel));
        verify(senderWallet, never()).withdraw(any());
        verify(receiverWallet, never()).deposit(any());
        verify(userDao, never()).save(sender);
        verify(userDao, never()).save(receiver);
    }

    @Test
    void expectReceiverNotFoundOnTransaction() throws InsufficientBalanceException, InvalidAmountException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        TransactionRequestModel requestModel = new TransactionRequestModel(1, "receiver", 2,new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()-> transactionService.transact(requestModel));

        verify(senderWallet, times(0)).withdraw(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
    }

    @Test
    void expectSameWalletsExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException {
        Wallet senderWallet = spy(new Wallet(1, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        TransactionRequestModel requestModel = new TransactionRequestModel(1, "sender", 1,new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(walletDao.findById(1)).thenReturn(Optional.of(senderWallet));

        assertThrows(SameWalletsForTransactionException.class,()-> transactionService.transact(requestModel));

        verify(senderWallet, times(0)).deposit(requestModel.getMoney());
        verify(senderWallet, times(0)).withdraw(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
    }

    @Test
    void expectSenderWalletNotFoundExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet receiverWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, new ArrayList<>());
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, Arrays.asList(receiverWallet));
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(2)).thenReturn(Optional.of(receiverWallet));

        assertThrows(WalletNotFoundException.class,()-> transactionService.transact(requestModel));
        verify(receiverWallet, times(0)).deposit(requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
        verify(userDao, times(0)).save(receiver);
    }

    @Test
    void expectReceiverWalletNotFoundExceptionOnTransaction() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, SameWalletsForTransactionException, WalletNotFoundException {
        Wallet senderWallet = spy(new Wallet(2, new Money(0, Currency.INR)));
        User sender = new User(1,"sender", "senderPassword", Country.INDIA, Arrays.asList(senderWallet));
        User receiver = new User(2,"receiver", "receiverPassword", Country.INDIA, new ArrayList<>());
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"receiver", 2, new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(walletDao.findById(2)).thenReturn(Optional.of(senderWallet));

        assertThrows(WalletNotFoundException.class,()-> transactionService.transact(requestModel));
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
        TransactionRequestModel requestModel = spy(new TransactionRequestModel(1,"sender", 2, new Money(90.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(walletDao.findById(1)).thenReturn(Optional.of(firstSenderWallet));
        when(walletDao.findById(2)).thenReturn(Optional.of(secondSenderWallet));

        transactionService.transact(requestModel);

        verify(firstSenderWallet, times(1)).deposit(new Money(100.0,Currency.INR));
        verify(firstSenderWallet, times(1)).withdraw(requestModel.getMoney());
        verify(secondSenderWallet, times(1)).deposit(requestModel.getMoney());
        verify(userDao, times(2)).save(sender);
    }

    @Test
    void expectAllTransactions() {
        User sender = new User("sender","testPassword", Country.INDIA);
        User receiver = new User("receiver","testPassword", Country.INDIA);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Wallet senderWallet = new Wallet(1, new Money(1000.0, Currency.INR));
        Wallet receiverWallet = new Wallet(2, new Money(0, Currency.INR));
        Transaction firstTransaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR), sender, 1, receiver, 2, new Money(0, Currency.INR));
        Transaction secondTransaction = new Transaction(LocalDateTime.now(),new Money(200, Currency.INR), sender, 1,receiver, 2, new Money(0, Currency.INR));
        List<Transaction> transactions = Arrays.asList(firstTransaction, secondTransaction);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionDao.findTransactionsOfUser(sender)).thenReturn(transactions);

        List<TransactionsResponseModel> response = transactionService.allTransactions();

        assertEquals(2, response.size());
        verify(userDao, times(1)).findByUserName("sender");
        verify(transactionDao, times(1)).findTransactionsOfUser(sender);
    }

    @Test
    void expectAllTransactionsDateBased() {
        User sender = new User("sender", "senderPassword", Country.INDIA);
        User receiver = new User("receiver", "receiverPassword", Country.INDIA);
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Wallet senderWallet = new Wallet(1, new Money(1000.0, Currency.INR));
        Wallet receiverWallet = new Wallet(2, new Money(0, Currency.INR));
        Transaction transaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR) , sender, 1, receiver, 2, new Money(0, Currency.INR));
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionDao.findTransactionsOfUserDateBased(sender, startDate, endDate)).thenReturn(transactions);

        List<TransactionsResponseModel> response = transactionService.allTransactionsDateBased(startDate.toLocalDate(), endDate.toLocalDate());

        assertEquals(1, response.size());
        verify(transactionDao, times(1)).findTransactionsOfUserDateBased(sender,startDate,endDate);
    }

    @Test
    void expectAllTransactionsDateBasedDifferentFromAllTransaction() {
        User sender = new User("sender", "senderPassword", Country.INDIA);
        User receiver = new User("receiver", "receiverPassword", Country.INDIA);
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Transaction firstTransaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR) , sender, 1, receiver, 2, new Money(0, Currency.INR));
        Transaction secondTransaction = new Transaction(LocalDateTime.now().minusDays(2), new Money(100, Currency.INR) , sender, 1, receiver, 2, new Money(0, Currency.INR));
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.add(firstTransaction);
        allTransactions.add(secondTransaction);
        List<Transaction> transactionsFiltered = new ArrayList<>();
        transactionsFiltered.add(firstTransaction);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionDao.findTransactionsOfUserDateBased(sender, startDate, endDate)).thenReturn(transactionsFiltered);
        when(transactionDao.findTransactionsOfUser(sender)).thenReturn(allTransactions);

        List<TransactionsResponseModel> responseDateBased = transactionService.allTransactionsDateBased(startDate.toLocalDate(), endDate.toLocalDate());
        List<TransactionsResponseModel> responseWithoutDate = transactionService.allTransactions();

        assertEquals(1, responseDateBased.size());
        assertEquals(2, responseWithoutDate.size());
        verify(transactionDao, times(1)).findTransactionsOfUser(sender);
        verify(transactionDao, times(1)).findTransactionsOfUserDateBased(sender,startDate, endDate);
    }

}
