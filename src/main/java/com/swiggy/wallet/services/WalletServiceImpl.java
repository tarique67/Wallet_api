package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.WalletDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletDAO walletDao;

    @Override
    public Wallet create(Wallet wallet) {
        return walletDao.save(wallet);
    }

    @Override
    public List<WalletResponseModel> getAllWallets() {
        List<Wallet> wallets = walletDao.findAll();
        List<WalletResponseModel> response = new ArrayList<>();
        for(Wallet wallet : wallets){
            response.add(new WalletResponseModel(wallet.getWalletId(), wallet.getMoney()));
        }
        return response;
    }

    @Override
    public Wallet deposit(int walletId, WalletRequestModel requestModel) throws InvalidAmountException {
        Wallet wallet = walletDao.findById(walletId).orElseThrow(()-> new NoSuchElementException("Wallet Not Found"));

        wallet.deposit(requestModel.getMoney());

        walletDao.save(wallet);
        return wallet;
    }

    @Override
    public Wallet withdraw(int walletId, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException {
        Wallet wallet = walletDao.findById(walletId).orElseThrow(()-> new NoSuchElementException("Wallet Not Found"));

        wallet.withdraw(requestModel.getMoney());
        walletDao.save(wallet);

        return wallet;
    }
}
