package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.*;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.IntraWalletTransactionsDAO;
import com.swiggy.wallet.repository.InterWalletTransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.swiggy.wallet.responseModels.ResponseMessage.*;

@Service
public class TransactionsServicesImpl implements TransactionsService {

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
    public TransactionsResponseModel allTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));
        List<Integer> userWallets = user.getWallets().stream().map(Wallet::getWalletId).toList();

        List<IntraWalletTransaction> intraWalletTransactions = intraWalletTransactionsDAO.findByWallets(userWallets);
        List<InterWalletTransaction> interWalletTransactions = interWalletTransactionDao.findTransactionsOfUser(user);
        for(InterWalletTransaction interWalletTransaction : interWalletTransactions){
            intraWalletTransactions.remove(interWalletTransaction.getDeposit());
            intraWalletTransactions.remove(interWalletTransaction.getWithdrawal());
        }
        List<InterWalletTransactionResponseModel> interWalletTransactionResponseModels = interWalletTransactions.stream().map((transaction -> new InterWalletTransactionResponseModel(transaction.getInterWalletTransactionId(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getDeposit(), transaction.getWithdrawal(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return new TransactionsResponseModel(interWalletTransactionResponseModels, intraWalletTransactions);
    }

    @Override
    public TransactionsResponseModel allTransactionsDateBased(LocalDate startDate, LocalDate endDate) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));
        List<Integer> userWallets = user.getWallets().stream().map(Wallet::getWalletId).toList();

        List<InterWalletTransaction> interWalletTransactions = interWalletTransactionDao.findTransactionsOfUserDateBased(user,startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        List<IntraWalletTransaction> intraWalletTransactions = intraWalletTransactionsDAO.findByWalletsAndDate(userWallets, startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        for(InterWalletTransaction interWalletTransaction : interWalletTransactions){
            intraWalletTransactions.remove(interWalletTransaction.getDeposit());
            intraWalletTransactions.remove(interWalletTransaction.getWithdrawal());
        }
        List<InterWalletTransactionResponseModel> interWalletTransactionResponseModels = interWalletTransactions.stream().map((transaction -> new InterWalletTransactionResponseModel(transaction.getInterWalletTransactionId(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getDeposit(), transaction.getWithdrawal(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return new TransactionsResponseModel(interWalletTransactionResponseModels, intraWalletTransactions);
    }

}
