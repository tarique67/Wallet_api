package com.swiggy.wallet.responseModels;

import com.swiggy.wallet.entities.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsResponseModel {

    private String sender;
    private String receiver;
    private Money money;
}
