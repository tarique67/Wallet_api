package com.swiggy.wallet.controllers;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.responseModels.ResponseMessageModel;
import com.swiggy.wallet.responseModels.TransactionsResponseModel;
import com.swiggy.wallet.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transact")
    public ResponseEntity<ResponseMessageModel> transact(@RequestBody TransactionRequestModel transactionRequestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        String response = transactionService.transact(transactionRequestModel);
        return new ResponseEntity<>(new ResponseMessageModel(response), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<List<TransactionsResponseModel>> allTransactions(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate){
        if(startDate != null && endDate != null)
            return new ResponseEntity<>(transactionService.allTransactionsDateBased(startDate,endDate), HttpStatus.OK);
        return new ResponseEntity<>(transactionService.allTransactions(), HttpStatus.OK);
    }
}
