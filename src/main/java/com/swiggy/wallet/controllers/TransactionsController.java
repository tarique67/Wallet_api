package com.swiggy.wallet.controllers;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    @Autowired
    private TransactionsService transactionsService;

    @PostMapping("/inter-wallet-transaction")
    public ResponseEntity<InterWalletTransactionResponseModel> transact(@RequestBody InterWalletTransactionRequestModel interWalletTransactionRequestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        InterWalletTransactionResponseModel response = transactionsService.transact(interWalletTransactionRequestModel);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<TransactionsResponseModel> allTransactions(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate){
        if(startDate != null && endDate != null)
            return new ResponseEntity<>(transactionsService.allTransactionsDateBased(startDate,endDate), HttpStatus.OK);
        return new ResponseEntity<>(transactionsService.allTransactions(), HttpStatus.OK);
    }
}
