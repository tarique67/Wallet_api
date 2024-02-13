package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;


public interface WalletServices {

    Wallet deposit(WalletRequestModel requestModel) throws InvalidAmountException;

    Wallet withdraw(WalletRequestModel requestModel) throws InsufficientBalanceException;

    Wallet create(Wallet wallet);
}
