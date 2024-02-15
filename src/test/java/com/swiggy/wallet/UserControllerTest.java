package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.requestModels.UserRequestModel;
import com.swiggy.wallet.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void testRegisterUser() throws Exception {
        UserRequestModel userRequestModel = new UserRequestModel("testUser", "testPassword");
        User user = new User("testUser", "testPassword", new Wallet());

        when(userService.register(userRequestModel)).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("testUser"));
        verify(userService, times(1)).register(userRequestModel);
    }

    @Test
    void testRegisterUser_UserAlreadyExists() throws Exception {
        UserRequestModel userRequestModel = new UserRequestModel("testUser","testPassword");

        when(userService.register(userRequestModel)).thenThrow(UserAlreadyExistsException.class);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestModel)))
                .andExpect(status().isBadRequest());
    }

}
