package com.swiggy.wallet.responseModels;

import com.swiggy.wallet.entities.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterWalletTransactionResponseModel {

    private LocalDateTime timestamp;
    private String sender;
    private int senderWalletId;
    private String receiver;
    private int receiverWalletId;
    private Money money;
    private Money serviceCharge;
}
