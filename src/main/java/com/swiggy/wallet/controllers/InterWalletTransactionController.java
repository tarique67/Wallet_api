package com.swiggy.wallet.controllers;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.ResponseMessageModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.services.InterWalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/v1/transactions")
public class InterWalletTransactionController {

    @Autowired
    private InterWalletTransactionService interWalletTransactionService;

    @PostMapping("/inter-wallet-transaction")
    public ResponseEntity<InterWalletTransactionResponseModel> transact(@RequestBody InterWalletTransactionRequestModel interWalletTransactionRequestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        InterWalletTransactionResponseModel response = interWalletTransactionService.transact(interWalletTransactionRequestModel);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<List<InterWalletTransactionResponseModel>> allTransactions(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate){
        if(startDate != null && endDate != null)
            return new ResponseEntity<>(interWalletTransactionService.allTransactionsDateBased(startDate,endDate), HttpStatus.OK);
        return new ResponseEntity<>(interWalletTransactionService.allTransactions(), HttpStatus.OK);
    }
}
