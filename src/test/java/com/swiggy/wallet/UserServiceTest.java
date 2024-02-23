package com.swiggy.wallet;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.enums.Country;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.UserDAO;
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

import java.util.ArrayList;
import java.util.Optional;

import static com.swiggy.wallet.responseModels.ResponseMessage.USER_DELETED_SUCCESSFULLY;
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
        when(userDao.save(any())).thenReturn(new User("testUser", "encodedPassword", Country.INDIA));
        UserRequestModel userRequestModel = new UserRequestModel("testUser", "testPassword", Country.INDIA);

        User savedUser = userService.register(userRequestModel);

        assertEquals("testUser", savedUser.getUserName());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertNotNull(savedUser.getWallets());
        verify(userDao, times(1)).findByUserName("testUser");
        verify(passwordEncoder, times(1)).encode("testPassword");
        verify(userDao, times(1)).save(any());
    }

    @Test
    void expectUserAlreadyExistsException() {
        when(userDao.findByUserName("existingUser")).thenReturn(Optional.of(new User()));
        UserRequestModel userRequestModel = new UserRequestModel("existingUser", "password", Country.INDIA);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(userRequestModel);
        });
        verify(userDao, times(1)).findByUserName("existingUser");
        verify(userDao, never()).save(any());
    }

    @Test
    void expectDeleteUserSuccessfully() throws UserNotFoundException {
        String username = "testUser";
        User user = new User(username, "password", Country.INDIA);
        when(userDao.findByUserName(username)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String result = userService.delete();

        verify(userDao, times(1)).findByUserName(username);
        verify(userDao, times(1)).delete(user);
        assertEquals(USER_DELETED_SUCCESSFULLY, result);
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
    void expectWalletAddedToUser() throws UserNotFoundException {
        String username = "testUser";
        User user = new User(1,username, "password", Country.INDIA, new ArrayList<>());
        when(userDao.findByUserName(username)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        userService.addWallet(1);

        verify(userDao, times(1)).findByUserName(username);
        verify(userDao, times(1)).save(user);
        assertEquals(1, user.getWallets().size() );
    }

    @Test
    void expect2WalletsAddedToUser() throws UserNotFoundException {
        String username = "testUser";
        User user = new User(1,username, "password", Country.INDIA, new ArrayList<>());
        when(userDao.findByUserName(username)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        userService.addWallet(1);
        userService.addWallet(1);

        verify(userDao, times(2)).findByUserName(username);
        verify(userDao, times(2)).save(user);
        assertEquals(2, user.getWallets().size() );
    }

    @Test
    void expectUserNotFoundException() throws UserNotFoundException {
        String username = "testUser";
        User user = new User(1,username, "password", Country.INDIA, new ArrayList<>());
        when(userDao.findByUserName(username)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UserNotFoundException.class,()-> userService.addWallet(2));
    }
}
