package com.swiggy.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void expectDepositSuccessfulWithValidAmount() throws Exception {
        String depositRequestBody = "{\"money\": 100}";
        mockMvc.perform(put("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositRequestBody))
                .andExpect(status().isAccepted());
    }

    @Test
    void expectExceptionForInvalidDepositAmount() throws Exception {
        String requestBody = "{\"money\": -50}";
        mockMvc.perform(put("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expectWithdrawSuccessfulWithValidAmount() throws Exception {
        String depositRequestBody = "{\"money\": 100}";
        mockMvc.perform(put("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositRequestBody))
                .andExpect(status().isAccepted());

        String requestBody = "{\"money\": 50}";
        mockMvc.perform(put("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.balance").value(50));
    }

    @Test
    void expectExceptionForWithdrawIfBalanceInsufficient() throws Exception {
        String requestBody = "{\"money\": 50}";
        mockMvc.perform(put("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
