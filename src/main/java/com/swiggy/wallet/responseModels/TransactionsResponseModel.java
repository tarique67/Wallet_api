package com.swiggy.wallet.responseModels;

import com.swiggy.wallet.entities.IntraWalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsResponseModel {

    private List<InterWalletTransactionResponseModel> interWalletTransactions;
    private List<IntraWalletTransaction> intraWalletTransactions;
}
