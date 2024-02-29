package com.swiggy.wallet;

import com.swiggy.wallet.entities.*;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.repository.InterWalletTransactionDAO;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionsServicesImpl;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
public class TransactionsServiceTest {

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
    private TransactionsServicesImpl transactionService;

    @BeforeEach
    void setUp(){
        openMocks(this);
    }

    @Test
    void expectInterWalletTransactions() {
        User sender = new User("sender","testPassword", Country.INDIA);
        User receiver = new User("receiver","testPassword", Country.INDIA);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Wallet senderWallet = new Wallet(1, new Money(1000.0, Currency.INR));
        Wallet receiverWallet = new Wallet(2, new Money(0, Currency.INR));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        InterWalletTransaction firstInterWalletTransaction = new InterWalletTransaction(sender, 1, receiver, 2, new Money(0, Currency.INR), deposit, withdrawal);
        InterWalletTransaction secondInterWalletTransaction = new InterWalletTransaction(sender, 1,receiver, 2, new Money(0, Currency.INR), deposit, withdrawal);
        List<InterWalletTransaction> interWalletTransactions = Arrays.asList(firstInterWalletTransaction, secondInterWalletTransaction);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(interWalletTransactionDao.findTransactionsOfUser(sender)).thenReturn(interWalletTransactions);
        when(intraWalletTransactionsDAO.findByWallets(Arrays.asList(1))).thenReturn(Arrays.asList(withdrawal));

        TransactionsResponseModel response = transactionService.allTransactions();

        assertEquals(2, response.getInterWalletTransactions().size());
        assertEquals(0, response.getIntraWalletTransactions().size());
        verify(userDao, times(1)).findByUserName("sender");
        verify(interWalletTransactionDao, times(1)).findTransactionsOfUser(sender);
    }

    @Test
    void expectInterTransactionsDateBased() {
        User sender = new User("sender", "senderPassword", Country.INDIA);
        User receiver = new User("receiver", "receiverPassword", Country.INDIA);
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Wallet senderWallet = new Wallet(1, new Money(1000.0, Currency.INR));
        Wallet receiverWallet = new Wallet(2, new Money(0, Currency.INR));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        InterWalletTransaction interWalletTransaction = new InterWalletTransaction(sender, 1, receiver, 2, new Money(0, Currency.INR),deposit,withdrawal);
        List<InterWalletTransaction> interWalletTransactions = new ArrayList<>();
        interWalletTransactions.add(interWalletTransaction);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(interWalletTransactionDao.findTransactionsOfUserDateBased(sender, startDate, endDate)).thenReturn(interWalletTransactions);
        when(intraWalletTransactionsDAO.findByWallets(Arrays.asList(1))).thenReturn(Arrays.asList(withdrawal));

        TransactionsResponseModel response = transactionService.allTransactionsDateBased(startDate.toLocalDate(), endDate.toLocalDate());

        assertEquals(1, response.getInterWalletTransactions().size());
        assertEquals(0, response.getIntraWalletTransactions().size());
        verify(interWalletTransactionDao, times(1)).findTransactionsOfUserDateBased(sender,startDate,endDate);
    }

    @Test
    void expectAllTransactionsDateBasedDifferentFromAllTransaction() {
        User sender = new User("sender", "senderPassword", Country.INDIA);
        User receiver = new User("receiver", "receiverPassword", Country.INDIA);
        LocalDateTime startDate = LocalDate.of(2022, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2022, 1, 31).atTime(23, 59, 59);
        Wallet senderWallet = new Wallet(1, new Money(1000.0, Currency.INR));
        Wallet receiverWallet = new Wallet(2, new Money(0, Currency.INR));
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now());
        IntraWalletTransaction secondDeposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,receiverWallet, LocalDateTime.now().minusDays(2));
        IntraWalletTransaction secondWithdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,senderWallet, LocalDateTime.now().minusDays(2));
        InterWalletTransaction firstInterWalletTransaction = new InterWalletTransaction(sender, 1, receiver, 2, new Money(0, Currency.INR), deposit, withdrawal);
        InterWalletTransaction secondInterWalletTransaction = new InterWalletTransaction(sender, 1, receiver, 2, new Money(0, Currency.INR), secondDeposit, secondWithdrawal);
        List<InterWalletTransaction> allInterWalletTransactions = new ArrayList<>();
        allInterWalletTransactions.add(firstInterWalletTransaction);
        allInterWalletTransactions.add(secondInterWalletTransaction);
        List<InterWalletTransaction> transactionsFiltered = new ArrayList<>();
        transactionsFiltered.add(firstInterWalletTransaction);
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(interWalletTransactionDao.findTransactionsOfUserDateBased(sender, startDate, endDate)).thenReturn(transactionsFiltered);
        when(interWalletTransactionDao.findTransactionsOfUser(sender)).thenReturn(allInterWalletTransactions);
        when(intraWalletTransactionsDAO.findByWallets(Arrays.asList(1))).thenReturn(Arrays.asList(withdrawal,secondWithdrawal));

        TransactionsResponseModel responseDateBased = transactionService.allTransactionsDateBased(startDate.toLocalDate(), endDate.toLocalDate());
        TransactionsResponseModel responseWithoutDate = transactionService.allTransactions();

        assertEquals(1, responseDateBased.getInterWalletTransactions().size());
        assertEquals(2, responseWithoutDate.getInterWalletTransactions().size());
        verify(interWalletTransactionDao, times(1)).findTransactionsOfUser(sender);
        verify(interWalletTransactionDao, times(1)).findTransactionsOfUserDateBased(sender,startDate, endDate);
    }
}
