package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.WalletDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WalletServicesImpl implements WalletServices{

    @Autowired
    private WalletDAO walletDao;

    @Override
    public Wallet create(Wallet wallet) {
        return walletDao.save(wallet);
    }

    @Override
    public Wallet deposit(WalletRequestModel requestModel) throws InvalidAmountException {
        Wallet wallet = walletDao.findById(1).orElseThrow(()-> new NoSuchElementException("Wallet Not Found"));

        wallet.deposit(requestModel.getMoney());

        walletDao.save(wallet);
        return wallet;
    }

    @Override
    public Wallet withdraw(WalletRequestModel requestModel) throws InsufficientBalanceException {
        Wallet wallet = walletDao.findById(1).orElseThrow(()-> new NoSuchElementException("Wallet Not Found"));

        wallet.withdraw(requestModel.getMoney());
        walletDao.save(wallet);

        return wallet;
    }
}
