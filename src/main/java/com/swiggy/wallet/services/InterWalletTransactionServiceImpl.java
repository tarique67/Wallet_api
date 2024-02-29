package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.InterWalletTransaction;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.InterWalletTransactionDAO;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.swiggy.wallet.responseModels.ResponseMessage.*;
import static com.swiggy.wallet.responseModels.ResponseMessage.WALLETS_SAME_IN_TRANSACTION;

@Service
public class InterWalletTransactionServiceImpl implements InterWalletTransactionService{

    @Autowired
    private InterWalletTransactionDAO interWalletTransactionDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private WalletDAO walletDao;

    @Autowired
    private IntraWalletTransactionsDAO intraWalletTransactionsDAO;

    @Override
    public InterWalletTransactionResponseModel transact(InterWalletTransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userDao.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User "+ username + " not found."));
        User receiver = userDao.findByUserName(requestModel.getReceiverName()).orElseThrow(() -> new UserNotFoundException("User "+ requestModel.getReceiverName() + " not found."));
        Wallet senderWallet = walletDao.findById(requestModel.getSenderWalletId()).orElseThrow(()-> new WalletNotFoundException(SENDER_WALLET_NOT_FOUND));
        Wallet receiverWallet = walletDao.findById(requestModel.getReceiverWalletId()).orElseThrow(()-> new WalletNotFoundException(RECEIVER_WALLET_NOT_FOUND));

        if(!sender.getWallets().contains(senderWallet) || !receiver.getWallets().contains(receiverWallet))
            throw new WalletNotFoundException(WALLET_ID_DOES_NOT_MATCH);
        if(senderWallet.equals(receiverWallet))
            throw new SameWalletsForTransactionException(WALLETS_SAME_IN_TRANSACTION);

        InterWalletTransaction interWalletTransaction = senderWallet.transact(requestModel, sender, receiverWallet, receiver);

        userDao.save(sender);
        userDao.save(receiver);
        InterWalletTransaction savedTransaction = interWalletTransactionDao.save(interWalletTransaction);

        return new InterWalletTransactionResponseModel(savedTransaction.getInterWalletTransactionId(), username, requestModel.getSenderWalletId(), requestModel.getReceiverName(), requestModel.getReceiverWalletId(), savedTransaction.getDeposit(), savedTransaction.getWithdrawal(), interWalletTransaction.getServiceCharge());
    }
}
