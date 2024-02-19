package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;

import java.util.List;

public interface TransactionService {

    String transact(TransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException;
    List<TransactionsResponseModel> allTransactions();
}
