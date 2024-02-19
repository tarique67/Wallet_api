package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.TransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.swiggy.wallet.responseModels.ResponseMessage.TRANSACTION_SUCCESSFUL;

@Service
public class TransactionServicesImpl implements TransactionService{

    @Autowired
    private TransactionDAO transactionDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private WalletService walletService;

    @Override
    public List<TransactionsResponseModel> allTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<Transaction> transactions = transactionDao.findTransactionsOfUser(user);
        List<TransactionsResponseModel> response = transactions.stream().map((transaction -> new TransactionsResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getReceiver().getUserName(), transaction.getMoney()))).collect(Collectors.toList());

        return response;
    }

    @Override
    public List<TransactionsResponseModel> allTransactionsDateBased(LocalDate startDate, LocalDate endDate) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<Transaction> transactions = transactionDao.findTransactionsOfUserDateBased(user,startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        List<TransactionsResponseModel> response = transactions.stream().map((transaction -> new TransactionsResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getReceiver().getUserName(), transaction.getMoney()))).collect(Collectors.toList());

        return response;
    }

    @Override
    public String transact(TransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userDao.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User "+ username + " not found."));
        User receiver = userDao.findByUserName(requestModel.getReceiverName()).orElseThrow(() -> new UserNotFoundException("User "+ requestModel.getReceiverName() + " not found."));

        walletService.transact(sender.getWallet(), receiver.getWallet(), requestModel.getMoney());

        userDao.save(sender);
        userDao.save(receiver);

        Transaction transaction = new Transaction(LocalDateTime.now(),requestModel.getMoney(), sender, receiver);
        transactionDao.save(transaction);

        return TRANSACTION_SUCCESSFUL;
    }
}
