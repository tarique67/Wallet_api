package com.swiggy.wallet.services;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;

import java.time.LocalDate;
import java.util.List;

public interface InterWalletTransactionService {

    InterWalletTransactionResponseModel transact(InterWalletTransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException;
    List<InterWalletTransactionResponseModel> allTransactions();

    List<InterWalletTransactionResponseModel> allTransactionsDateBased(LocalDate startDate, LocalDate endDate);
}
