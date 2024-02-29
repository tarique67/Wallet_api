package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.IntraWalletTransaction;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.enums.IntraWalletTransactionType;
import com.swiggy.wallet.exceptions.SameWalletsForTransactionException;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.services.InterWalletTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.swiggy.wallet.responseModels.ResponseMessage.WALLETS_SAME_IN_TRANSACTION;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InterWalletTransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterWalletTransactionServiceImpl interWalletTransactionService;

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

        mockMvc.perform(post("/api/v1/inter-wallet-transactions")
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

        mockMvc.perform(post("/api/v1/inter-wallet-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(WALLETS_SAME_IN_TRANSACTION));
        verify(interWalletTransactionService, times(1)).transact(interWalletTransactionRequestModel);
    }
}
