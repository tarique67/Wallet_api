package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.repository.WalletDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletDAO walletDao;

    @Autowired
    private UserDAO userDao;

    @Override
    public Wallet create(Wallet wallet) {
        return walletDao.save(wallet);
    }

    @Override
    public List<WalletResponseModel> getAllWallets() {
        List<Wallet> wallets = walletDao.findAll();
        List<WalletResponseModel> response = new ArrayList<>();
        for(Wallet wallet : wallets){
            response.add(new WalletResponseModel(wallet.getMoney()));
        }
        return response;
    }

    @Override
    public WalletResponseModel deposit(String username, WalletRequestModel requestModel) throws InvalidAmountException, AuthenticationFailedException {
        User user = userDao.findByUserName(username).orElseThrow(() -> new AuthenticationFailedException("Username or password does not match."));

        user.getWallet().deposit(requestModel.getMoney());

        userDao.save(user);
        return new WalletResponseModel(user.getWallet().getMoney());
    }

    @Override
    public WalletResponseModel withdraw(String username, WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, AuthenticationFailedException {
        User user = userDao.findByUserName(username).orElseThrow(() -> new AuthenticationFailedException("Username or password does not match."));

        user.getWallet().withdraw(requestModel.getMoney());

        userDao.save(user);
        return new WalletResponseModel(user.getWallet().getMoney());
    }

    @Override
    public void transact(Wallet senderWallet, Wallet receiverWallet, Money money) throws InsufficientBalanceException, InvalidAmountException {
        senderWallet.withdraw(money);
        receiverWallet.deposit(money);
    }
}
