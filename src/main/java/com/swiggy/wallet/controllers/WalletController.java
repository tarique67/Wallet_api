package com.swiggy.wallet.controllers;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.services.WalletServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WalletController {

    @Autowired
    private WalletServices walletService;

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello(){
        return new ResponseEntity<>("Hello", HttpStatus.OK);
    }

    @PostMapping("/wallet")
    public ResponseEntity<Wallet> create(){
        Wallet returnedWallet = walletService.create(new Wallet());

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/deposit")
    public ResponseEntity<Wallet> deposit(@RequestBody WalletRequestModel requestModel) throws InvalidAmountException {
        Wallet returnedWallet = walletService.deposit(requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(@RequestBody WalletRequestModel requestModel) throws InsufficientBalanceException {
        Wallet returnedWallet = walletService.withdraw(requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }
}
