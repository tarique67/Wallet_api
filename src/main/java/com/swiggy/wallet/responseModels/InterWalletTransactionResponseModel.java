package com.swiggy.wallet.responseModels;

import com.swiggy.wallet.entities.IntraWalletTransaction;
import com.swiggy.wallet.entities.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterWalletTransactionResponseModel {

    private int interWalletTransactionId;
    private String sender;
    private int senderWalletId;
    private String receiver;
    private int receiverWalletId;
    private IntraWalletTransaction deposit;
    private IntraWalletTransaction withdrawal;
    private Money serviceCharge;
}
