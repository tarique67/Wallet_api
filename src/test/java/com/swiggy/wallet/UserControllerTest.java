package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.requestModels.UserRequestModel;
import com.swiggy.wallet.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static com.swiggy.wallet.responseModels.ResponseMessage.TRANSACTION_SUCCESSFUL;
import static com.swiggy.wallet.responseModels.ResponseMessage.USER_DELETED_SUCCESSFULLY;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(userService);
    }

    @Test
    void expectUserCreated() throws Exception {
        UserRequestModel userRequestModel = new UserRequestModel("testUser", "testPassword");
        User user = new User("testUser", "testPassword");

        when(userService.register(userRequestModel)).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("testUser"));
        verify(userService, times(1)).register(userRequestModel);
    }

    @Test
    void expectUserAlreadyExists() throws Exception {
        UserRequestModel userRequestModel = new UserRequestModel("testUser","testPassword");

        when(userService.register(userRequestModel)).thenThrow(UserAlreadyExistsException.class);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestModel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user")
    void expectUserDeleted() throws Exception {
        when(userService.delete()).thenReturn(USER_DELETED_SUCCESSFULLY);

        mockMvc.perform(delete("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(USER_DELETED_SUCCESSFULLY));
        verify(userService, times(1)).delete();
    }

    @Test
    @WithMockUser(username = "userNotFound")
    void expectUserNotFoundException() throws Exception {
        String username = "userNotFound";
        String errorMessage = "User "+username+" not be found.";

        when(userService.delete()).thenThrow(new UserNotFoundException(errorMessage));

        mockMvc.perform(delete("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(userService, times(1)).delete();
    }

    @Test
    @WithMockUser(username = "sender")
    void testTransactEndpoint() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel("sender", new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(userService.transact(transactionRequestModel)).thenReturn(TRANSACTION_SUCCESSFUL);

        mockMvc.perform(put("/api/v1/users/transact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(TRANSACTION_SUCCESSFUL));
    }
}
