package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.swiggy.wallet.responseModels.ResponseMessage.TRANSACTION_SUCCESSFUL;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel(1,"receiver", 2,new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.transact(transactionRequestModel)).thenReturn(TRANSACTION_SUCCESSFUL);

        mockMvc.perform(post("/api/v1/transactions/transact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(TRANSACTION_SUCCESSFUL));
        verify(transactionService, times(1)).transact(transactionRequestModel);
    }

    @Test
    @WithMockUser(username = "sender")
    void expectAllTransactionsOfUser() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel(1,"receiver",2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.allTransactions()).thenReturn(Arrays.asList(new TransactionsResponseModel(LocalDateTime.now(),"sender",1,"receiver",2, new Money(100, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
        verify(transactionService, times(1)).allTransactions();
    }

    @Test
    void expectUnauthorizedForAllTransactions() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel(1,"receiver",2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.allTransactions()).thenReturn(Arrays.asList(new TransactionsResponseModel(LocalDateTime.now(),"sender",1,"receiver",2, new Money(100, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
        verify(transactionService, times(0)).allTransactions();
    }

    @Test
    @WithMockUser(username = "sender")
    public void expectAllTransactionsDateBased() throws Exception {
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 1, 31);
        List<TransactionsResponseModel> mockResponse = Arrays.asList(
                new TransactionsResponseModel(LocalDateTime.now(),"sender", 1, "receiver1", 2, new Money(100, Currency.INR)),
                new TransactionsResponseModel(LocalDateTime.now(),"sender", 1, "receiver2", 3, new Money(200, Currency.INR))
        );
        when(transactionService.allTransactionsDateBased(startDate, endDate)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sender").value("sender"))
                .andExpect(jsonPath("$[0].receiver").value("receiver1"))
                .andExpect(jsonPath("$[0].money.amount").value(100))
                .andExpect(jsonPath("$[1].sender").value("sender"))
                .andExpect(jsonPath("$[1].receiver").value("receiver2"))
                .andExpect(jsonPath("$[1].money.amount").value(200));
        verify(transactionService, times(1)).allTransactionsDateBased(startDate,endDate);
    }

    @Test
    void expectUnauthorizedForAllTransactionsDateBased() throws Exception {
        TransactionRequestModel transactionRequestModel = new TransactionRequestModel(1, "receiver", 2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(transactionRequestModel);
        when(transactionService.allTransactionsDateBased(LocalDate.now(), LocalDate.now())).thenReturn(Arrays.asList(new TransactionsResponseModel(LocalDateTime.now() ,"sender1", 1, "receiver1",2, new Money(100, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
        verify(transactionService, times(0)).allTransactionsDateBased(LocalDate.now(), LocalDate.now());
    }
}
