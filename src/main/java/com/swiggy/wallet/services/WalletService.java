package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.exceptions.WalletNotFoundException;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;

import java.util.List;


public interface WalletService {

    WalletResponseModel deposit(int walletId, String username, WalletRequestModel requestModel) throws InvalidAmountException, AuthenticationFailedException, WalletNotFoundException;

    WalletResponseModel withdraw(int walletId, String username, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, AuthenticationFailedException, WalletNotFoundException;

    Wallet create(Wallet wallet);

    List<WalletResponseModel> getAllWallets();

}
