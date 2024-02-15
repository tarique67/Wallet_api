package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;

import java.util.List;


public interface WalletService {

    Wallet deposit(String walletId, WalletRequestModel requestModel) throws InvalidAmountException, AuthenticationFailedException;

    Wallet withdraw(String walletId, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, AuthenticationFailedException;

    Wallet create(Wallet wallet);

    List<WalletResponseModel> getAllWallets();
}
