package com.swiggy.wallet.responseModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseModel {

    private String message;
    private LocalDateTime timeStamp = LocalDateTime.now();

    public TransactionResponseModel(String message) {
        this.message = message;
    }
}
