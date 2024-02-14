package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.entities.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;

import java.util.List;


public interface WalletService {

    Wallet deposit(int walletId, WalletRequestModel requestModel) throws InvalidAmountException;

    Wallet withdraw(int walletId, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException;

    Wallet create(Wallet wallet);

    List<WalletResponseModel> getAllWallets();
}
