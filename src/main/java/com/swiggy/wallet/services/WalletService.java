package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;

import java.util.List;


public interface WalletService {

    WalletResponseModel deposit(String username, WalletRequestModel requestModel) throws InvalidAmountException, AuthenticationFailedException;

    WalletResponseModel withdraw(String username, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, AuthenticationFailedException;

    Wallet create(Wallet wallet);

    List<WalletResponseModel> getAllWallets();

    void transact(Wallet wallet, Wallet wallet1, Money money) throws InsufficientBalanceException, InvalidAmountException;
}
