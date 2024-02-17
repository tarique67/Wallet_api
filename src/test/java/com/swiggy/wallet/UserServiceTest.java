package com.swiggy.wallet;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.requestModels.UserRequestModel;
import com.swiggy.wallet.services.UserServiceImpl;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserDAO userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletService walletService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp(){
        openMocks(this);
    }

    @Test
    void expectUserCreated() throws UserAlreadyExistsException {
        when(userDao.findByUserName("testUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userDao.save(any())).thenReturn(new User("testUser", "encodedPassword"));
        UserRequestModel userRequestModel = new UserRequestModel("testUser", "testPassword");

        User savedUser = userService.register(userRequestModel);

        assertEquals("testUser", savedUser.getUserName());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertNotNull(savedUser.getWallet());
        verify(userDao, times(1)).findByUserName("testUser");
        verify(passwordEncoder, times(1)).encode("testPassword");
        verify(userDao, times(1)).save(any());
    }

    @Test
    void expectUserAlreadyExistsException() {
        when(userDao.findByUserName("existingUser")).thenReturn(Optional.of(new User()));
        UserRequestModel userRequestModel = new UserRequestModel("existingUser", "password");

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(userRequestModel);
        });
        verify(userDao, times(1)).findByUserName("existingUser");
        verify(userDao, never()).save(any());
    }

    @Test
    void expectDeleteUserSuccessfully() throws UserNotFoundException {
        String username = "testUser";
        User user = new User(username, "password");
        when(userDao.findByUserName(username)).thenReturn(Optional.of(user));
        String expectedResult = "User " + username + " deleted successfully.";
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String result = userService.delete();

        verify(userDao, times(1)).findByUserName(username);
        verify(userDao, times(1)).delete(user);
        assertEquals(expectedResult, result);
    }

    @Test
    void expectDeleteUserThrowsUserNotFoundException() {
        String username = "nonExistingUser";
        when(userDao.findByUserName(username)).thenReturn(Optional.empty());
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UserNotFoundException.class, () -> userService.delete());
        verify(userDao, times(1)).findByUserName(username);
        verify(userDao, never()).delete(any());
    }

    @Test
    void expectTransactionSuccessful() throws InsufficientBalanceException, InvalidAmountException {
        User sender = new User("sender", "senderPassword");
        User receiver = new User("receiver", "receiverPassword");
        TransactionRequestModel requestModel = new TransactionRequestModel("receiver", new Money(100.0, Currency.INR));
        when(authentication.getName()).thenReturn("sender");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userDao.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userDao.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        userService.transact(requestModel);

        verify(walletService, times(1)).transact(sender.getWallet(), receiver.getWallet(), requestModel.getMoney());
        verify(userDao, times(1)).save(sender);
        verify(userDao, times(1)).save(receiver);
    }
}
