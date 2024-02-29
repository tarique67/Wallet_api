package com.swiggy.wallet.services;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;

import java.time.LocalDate;

public interface TransactionsService {

    InterWalletTransactionResponseModel transact(InterWalletTransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException;
    TransactionsResponseModel allTransactions();

    TransactionsResponseModel allTransactionsDateBased(LocalDate startDate, LocalDate endDate);
}
