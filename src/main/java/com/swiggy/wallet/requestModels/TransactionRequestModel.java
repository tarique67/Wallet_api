package com.swiggy.wallet.requestModels;

import com.swiggy.wallet.entities.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestModel {

    private String receiverName;
    private Money money;
}
