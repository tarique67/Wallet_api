package com.swiggy.wallet;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.UserRequestModel;
import com.swiggy.wallet.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
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
        when(userDao.save(any())).thenReturn(new User("testUser", "encodedPassword", new Wallet()));
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
    }

}
