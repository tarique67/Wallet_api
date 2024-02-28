package com.swiggy.wallet.services;

import com.swiggy.wallet.currencyConverterGrpcClient.CurrencyConverter;
import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.entities.Transaction;
import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.enums.Currency;
import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.repository.TransactionDAO;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.repository.WalletDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import proto.ConvertResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.swiggy.wallet.constants.Constants.SERVICE_CHARGE_IN_INR;
import static com.swiggy.wallet.responseModels.ResponseMessage.*;

@Service
public class TransactionServicesImpl implements TransactionService{

    @Autowired
    private TransactionDAO transactionDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private WalletDAO walletDao;

    @Autowired
    private WalletService walletService;

    @Override
    public List<TransactionsResponseModel> allTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<Transaction> transactions = transactionDao.findTransactionsOfUser(user);
        List<TransactionsResponseModel> response = transactions.stream().map((transaction -> new TransactionsResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getMoney(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return response;
    }

    @Override
    public List<TransactionsResponseModel> allTransactionsDateBased(LocalDate startDate, LocalDate endDate) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("Username not found."));

        List<Transaction> transactions = transactionDao.findTransactionsOfUserDateBased(user,startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        List<TransactionsResponseModel> response = transactions.stream().map((transaction -> new TransactionsResponseModel(transaction.getTimestamp(), transaction.getSender().getUserName(), transaction.getSenderWalletId(), transaction.getReceiver().getUserName(), transaction.getReceiverWalletId(), transaction.getMoney(), transaction.getServiceCharge()))).collect(Collectors.toList());

        return response;
    }

    @Override
    public String transact(TransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userDao.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User "+ username + " not found."));
        User receiver = userDao.findByUserName(requestModel.getReceiverName()).orElseThrow(() -> new UserNotFoundException("User "+ requestModel.getReceiverName() + " not found."));
        Wallet senderWallet = walletDao.findById(requestModel.getSenderWalletId()).orElseThrow(()-> new WalletNotFoundException(SENDER_WALLET_NOT_FOUND));
        Wallet receiverWallet = walletDao.findById(requestModel.getReceiverWalletId()).orElseThrow(()-> new WalletNotFoundException(RECEIVER_WALLET_NOT_FOUND));

        if(!sender.getWallets().contains(senderWallet) || !receiver.getWallets().contains(receiverWallet))
            throw new WalletNotFoundException(WALLET_ID_DOES_NOT_MATCH);
        if(senderWallet.equals(receiverWallet))
            throw new SameWalletsForTransactionException(WALLETS_SAME_IN_TRANSACTION);

        CurrencyConverter converter = new CurrencyConverter();
        ConvertResponse res = converter.convertMoney(requestModel.getMoney(), senderWallet.getMoney().getCurrency(), receiverWallet.getMoney().getCurrency());

        double serviceCharge = res.getServiceCharge().getAmount();

        if(serviceCharge >= requestModel.getMoney().getAmount())
            throw new InvalidAmountException(AMOUNT_LESS_THAN_SERVICE_CHARGE);

        senderWallet.withdraw(requestModel.getMoney());

        if(serviceCharge > 0.0)
            requestModel.getMoney().subtract(new Money(serviceCharge, requestModel.getMoney().getCurrency()));

        receiverWallet.deposit(requestModel.getMoney());

        userDao.save(sender);
        userDao.save(receiver);
        Transaction transaction = new Transaction(LocalDateTime.now(),requestModel.getMoney(), sender, senderWallet.getWalletId(), receiver, receiverWallet.getWalletId(), SERVICE_CHARGE_IN_INR);
        transactionDao.save(transaction);

        return TRANSACTION_SUCCESSFUL;
    }
}
