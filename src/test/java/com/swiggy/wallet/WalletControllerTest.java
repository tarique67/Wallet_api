package com.swiggy.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        reset(walletService);
    }

    @Test
    @WithMockUser(username = "user")
    void expectAmountDepositedSuccessfully() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(100, Currency.INR));
        WalletResponseModel responseModel = new WalletResponseModel(1,new Money(100, Currency.INR));
        when(walletService.deposit(anyInt(), anyString(), any())).thenReturn(responseModel);

        mockMvc.perform(post("/api/v1/wallets/1/intra-wallet-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("type", "deposit")
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.money.amount").value("100.0"));
        verify(walletService, times(1)).deposit(anyInt(), anyString(),any());
    }

    @Test
    void expectUnauthorizedOnDeposit() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(100, Currency.INR));
        WalletResponseModel responseModel = new WalletResponseModel(1, new Money(100, Currency.INR));
        when(walletService.deposit(anyInt(), anyString(), any())).thenReturn(responseModel);

        mockMvc.perform(put("/api/v1/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isUnauthorized());
        verify(walletService, never()).deposit(anyInt(), anyString(),any());
    }

    @Test
    @WithMockUser(username = "user")
    void expectWithdrawalSuccessful() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));
        String requestBody = objectMapper.writeValueAsString(requestModel);
        WalletResponseModel responseModel = new WalletResponseModel(1, new Money(50, Currency.INR));
        when(walletService.withdraw(anyInt(), anyString(), any())).thenReturn(responseModel);

        mockMvc.perform(post("/api/v1/wallets/1/intra-wallet-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("type", "withdraw")
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.money.amount").value("50.0"));
        verify(walletService, times(1)).withdraw(anyInt(), anyString(), any(WalletRequestModel.class));
    }

    @Test
    void expectUnauthorizedOnWithdrawal() throws Exception {
        WalletRequestModel requestModel = new WalletRequestModel(new Money(50, Currency.INR));
        String requestBody = objectMapper.writeValueAsString(requestModel);

        mockMvc.perform(put("/api/v1/wallets/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
        verify(walletService, never()).withdraw(anyInt(), anyString(), any(WalletRequestModel.class));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void expectWalletListForUser() throws Exception {
        WalletResponseModel firstWallet = new WalletResponseModel();
        WalletResponseModel secondWallet = new WalletResponseModel();
        when(walletService.getAllWallets()).thenReturn(Arrays.asList(firstWallet, secondWallet));

        MvcResult mockResult = mockMvc.perform(get("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String responseContent = mockResult.getResponse().getContentAsString();
        WalletResponseModel[] walletResponse = objectMapper.readValue(responseContent, WalletResponseModel[].class);

        verify(walletService, times(1)).getAllWallets();
        assertEquals(2, walletResponse.length);
    }

    @Test
    void expectUnauthorizedForWalletList() throws Exception {
        WalletResponseModel firstWallet = new WalletResponseModel();
        WalletResponseModel secondWallet = new WalletResponseModel();
        when(walletService.getAllWallets()).thenReturn(Arrays.asList(firstWallet, secondWallet));

        mockMvc.perform(get("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(walletService, never()).getAllWallets();
    }
}
