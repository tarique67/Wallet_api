package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    String transact(TransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException;
    List<TransactionsResponseModel> allTransactions();

    List<TransactionsResponseModel> allTransactionsDateBased(LocalDate startDate, LocalDate endDate);
}
