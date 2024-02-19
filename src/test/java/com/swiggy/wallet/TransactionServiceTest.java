package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.TransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionServicesImpl;
import com.swiggy.wallet.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
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
    private WalletService walletService;

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
    void expectTransactionSuccessful() throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException {
        User sender = new User("sender", "senderPassword");
        User receiver = new User("receiver", "receiverPassword");
        TransactionRequestModel requestModel = spy(new TransactionRequestModel("receiver", new Money(100.0, Currency.INR)));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        transactionService.transact(requestModel);

        verify(walletService, times(1)).transact(sender.getWallet(), receiver.getWallet(), requestModel.getMoney());
        verify(userDao, times(1)).save(sender);
        verify(userDao, times(1)).save(receiver);
    }

    @Test
    void expectReceiverNotFoundOnTransaction() throws InsufficientBalanceException, InvalidAmountException {
        User sender = new User("sender", "senderPassword");
        User receiver = new User("receiver", "receiverPassword");
        TransactionRequestModel requestModel = new TransactionRequestModel("receiver", new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()-> transactionService.transact(requestModel));
        verify(walletService, times(0)).transact(sender.getWallet(), receiver.getWallet(), requestModel.getMoney());
        verify(userDao, times(0)).save(sender);
        verify(userDao, times(0)).save(receiver);
    }

    @Test
    void expectAllTransactions() {
        User sender = new User("sender","testPassword");
        User receiver = new User("receiver","testPassword");
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Transaction firstTransaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR), sender, receiver);
        Transaction secondTransaction = new Transaction(LocalDateTime.now(),new Money(200, Currency.INR), sender, receiver);
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
        User sender = new User("sender", "senderPassword");
        User receiver = new User("receiver", "receiverPassword");
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Transaction transaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR) , sender, receiver);
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
        User sender = new User("sender", "senderPassword");
        User receiver = new User("receiver", "receiverPassword");
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Transaction firstTransaction = new Transaction(LocalDateTime.now(), new Money(100, Currency.INR) , sender, receiver);
        Transaction secondTransaction = new Transaction(LocalDateTime.now().minusDays(2), new Money(100, Currency.INR) , sender, receiver);
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
