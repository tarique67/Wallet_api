package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;


public interface WalletService {

    Wallet deposit(WalletRequestModel requestModel) throws InvalidAmountException;

    Wallet withdraw(WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException;

    Wallet create(Wallet wallet);
}
