package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.ResponseMessage;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionService;
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

import java.util.Arrays;

import static com.swiggy.wallet.responseModels.ResponseMessage.TRANSACTION_SUCCESSFUL;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(transactionService);
    }

    @Test
    @WithMockUser(username = "sender")
    void expectTransactionSuccessful() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel("receiver", new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.transact(transactionRequestModel)).thenReturn(TRANSACTION_SUCCESSFUL);

        mockMvc.perform(post("/api/v1/transactions/transact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(TRANSACTION_SUCCESSFUL));
    }

    @Test
    @WithMockUser(username = "sender")
    void expectAllTransactionsOfUser() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel("receiver", new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.allTransactions()).thenReturn(Arrays.asList(new TransactionsResponseModel("sender1","receiver1", new Money(100, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
        verify(transactionService, times(1)).allTransactions();
    }

    @Test
    void expectUnauthorized() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel("receiver", new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.allTransactions()).thenReturn(Arrays.asList(new TransactionsResponseModel("sender1","receiver1", new Money(100, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
        verify(transactionService, times(0)).allTransactions();
    }
}
