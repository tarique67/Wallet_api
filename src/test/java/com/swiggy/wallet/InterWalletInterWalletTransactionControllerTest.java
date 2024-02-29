package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.InterWalletTransaction;
import com.swiggy.wallet.entities.IntraWalletTransaction;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.exceptions.SameWalletsForTransactionException;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.services.InterWalletTransactionService;
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
import static com.swiggy.wallet.responseModels.ResponseMessage.WALLETS_SAME_IN_TRANSACTION;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InterWalletInterWalletTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterWalletTransactionService interWalletTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(interWalletTransactionService);
    }

    @Test
    @WithMockUser(username = "sender")
    void expectTransactionSuccessful() throws Exception {
        InterWalletTransactionRequestModel interWalletTransactionRequestModel = new InterWalletTransactionRequestModel(1,"receiver", 2,new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(interWalletTransactionRequestModel);
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(90, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        InterWalletTransactionResponseModel expected = new InterWalletTransactionResponseModel(1,"sender", 1, "receiver", 2, deposit, withdrawal, new Money(10.0, Currency.INR));
        when(interWalletTransactionService.transact(interWalletTransactionRequestModel)).thenReturn(expected);

        mockMvc.perform(post("/api/v1/transactions/inter-wallet-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.deposit.money.amount").value(90))
                .andExpect(jsonPath("$.withdrawal.money.amount").value(100))
                .andExpect(jsonPath("$.serviceCharge.amount").value(10));
        verify(interWalletTransactionService, times(1)).transact(interWalletTransactionRequestModel);
    }

    @Test
    @WithMockUser(username = "sender")
    void expectSameWalletExceptionIfTransferringToSameAccount() throws Exception {
        InterWalletTransactionRequestModel interWalletTransactionRequestModel = new InterWalletTransactionRequestModel(1,"sender", 1,new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(interWalletTransactionRequestModel);
        when(interWalletTransactionService.transact(interWalletTransactionRequestModel)).thenThrow(new SameWalletsForTransactionException(WALLETS_SAME_IN_TRANSACTION));

        mockMvc.perform(post("/api/v1/transactions/inter-wallet-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(WALLETS_SAME_IN_TRANSACTION));
        verify(interWalletTransactionService, times(1)).transact(interWalletTransactionRequestModel);
    }

    @Test
    @WithMockUser(username = "sender")
    void expectAllTransactionsOfUser() throws Exception {
        InterWalletTransactionRequestModel interWalletTransactionRequestModel = new InterWalletTransactionRequestModel(1,"receiver",2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(interWalletTransactionRequestModel);
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        when(interWalletTransactionService.allTransactions()).thenReturn(Arrays.asList(new InterWalletTransactionResponseModel(1, "sender",1,"receiver",2, deposit, withdrawal, new Money(0, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
        verify(interWalletTransactionService, times(1)).allTransactions();
    }

    @Test
    void expectUnauthorizedForAllTransactions() throws Exception {
        InterWalletTransactionRequestModel interWalletTransactionRequestModel = new InterWalletTransactionRequestModel(1,"receiver",2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(interWalletTransactionRequestModel);
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        when(interWalletTransactionService.allTransactions()).thenReturn(Arrays.asList(new InterWalletTransactionResponseModel(1,"sender",1,"receiver",2, deposit, withdrawal, new Money(0, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
        verify(interWalletTransactionService, times(0)).allTransactions();
    }

    @Test
    @WithMockUser(username = "sender")
    public void expectAllTransactionsDateBased() throws Exception {
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 1, 31);
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction secondDeposit = new IntraWalletTransaction(new Money(200, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction secondWithdrawal = new IntraWalletTransaction(new Money(200, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        List<InterWalletTransactionResponseModel> mockResponse = Arrays.asList(
                new InterWalletTransactionResponseModel(1,"sender", 1, "receiver1", 2, deposit, withdrawal, new Money(0, Currency.INR)),
                new InterWalletTransactionResponseModel(2,"sender", 1, "receiver2", 3, secondDeposit, secondWithdrawal, new Money(0, Currency.INR))
        );
        when(interWalletTransactionService.allTransactionsDateBased(startDate, endDate)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sender").value("sender"))
                .andExpect(jsonPath("$[0].receiver").value("receiver1"))
                .andExpect(jsonPath("$[0].deposit.money.amount").value(100))
                .andExpect(jsonPath("$[1].sender").value("sender"))
                .andExpect(jsonPath("$[1].receiver").value("receiver2"))
                .andExpect(jsonPath("$[1].deposit.money.amount").value(200));
        verify(interWalletTransactionService, times(1)).allTransactionsDateBased(startDate,endDate);
    }

    @Test
    void expectUnauthorizedForAllTransactionsDateBased() throws Exception {
        InterWalletTransactionRequestModel interWalletTransactionRequestModel = new InterWalletTransactionRequestModel(1, "receiver", 2, new Money(100, Currency.INR));
        String requestJson = objectMapper.writeValueAsString(interWalletTransactionRequestModel);
        IntraWalletTransaction deposit = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.DEPOSIT,new Wallet(), LocalDateTime.now());
        IntraWalletTransaction withdrawal = new IntraWalletTransaction(new Money(100, Currency.INR), IntraWalletTransactionType.WITHDRAW,new Wallet(), LocalDateTime.now());
        when(interWalletTransactionService.allTransactionsDateBased(LocalDate.now(), LocalDate.now())).thenReturn(Arrays.asList(new InterWalletTransactionResponseModel(1,"sender1", 1, "receiver1",2, deposit, withdrawal, new Money(0, Currency.INR))));

        mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
        verify(interWalletTransactionService, times(0)).allTransactionsDateBased(LocalDate.now(), LocalDate.now());
    }
}
