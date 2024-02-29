package com.swiggy.wallet.controllers;

import com.swiggy.wallet.exceptions.*;
import com.swiggy.wallet.requestModels.InterWalletTransactionRequestModel;
import com.swiggy.wallet.responseModels.InterWalletTransactionResponseModel;
import com.swiggy.wallet.services.InterWalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/inter-wallet-transactions")
public class InterWalletTransactionController {

    @Autowired
    private InterWalletTransactionService interWalletTransactionService;

    @PostMapping("")
    public ResponseEntity<InterWalletTransactionResponseModel> transact(@RequestBody InterWalletTransactionRequestModel interWalletTransactionRequestModel) throws InsufficientBalanceException, InvalidAmountException, UserNotFoundException, WalletNotFoundException, SameWalletsForTransactionException {
        InterWalletTransactionResponseModel response = interWalletTransactionService.transact(interWalletTransactionRequestModel);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
