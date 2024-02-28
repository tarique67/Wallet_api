package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.*;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
import com.swiggy.wallet.repository.InterWalletTransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.swiggy.wallet.responseModels.ResponseMessage.*;

@Service
public class InterWalletTransactionServicesImpl implements InterWalletTransactionService {

    @Autowired
    private InterWalletTransactionDAO interWalletTransactionDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private WalletDAO walletDao;

    @Autowired
    private WalletService walletService;

    @Autowired
    private IntraWalletTransactionsDAO intraWalletTransactionsDAO;

    @Override
    public List<InterWalletTransactionResponseModel> allTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<InterWalletTransaction> interWalletTransactions = interWalletTransactionDao.findTransactionsOfUser(user);
        List<InterWalletTransactionResponseModel> response = interWalletTransactions.stream().map((transaction -> new InterWalletTransactionResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getMoney(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return response;
    }

    @Override
    public List<InterWalletTransactionResponseModel> allTransactionsDateBased(LocalDate startDate, LocalDate endDate) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<InterWalletTransaction> interWalletTransactions = interWalletTransactionDao.findTransactionsOfUserDateBased(user,startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        List<InterWalletTransactionResponseModel> response = interWalletTransactions.stream().map((transaction -> new InterWalletTransactionResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getMoney(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return response;
    }

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
        interWalletTransactionDao.save(interWalletTransaction);

        return new InterWalletTransactionResponseModel(LocalDateTime.now(), username, requestModel.getSenderWalletId(), requestModel.getReceiverName(), requestModel.getReceiverWalletId(), requestModel.getMoney(), interWalletTransaction.getServiceCharge());
    }
}
