package com.swiggy.wallet.controllers;

import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.entities.WalletRequestModel;
import com.swiggy.wallet.entities.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello(){
        return new ResponseEntity<>("Hello", HttpStatus.OK);
    }

    @PostMapping("/wallet")
    public ResponseEntity<Wallet> create(){
        Wallet returnedWallet = walletService.create(new Wallet());

        return new ResponseEntity<>(returnedWallet, HttpStatus.CREATED);
    }

    @PutMapping("/{wallet_id}/deposit")
    public ResponseEntity<Wallet> deposit(@PathVariable("wallet_id") int walletId, @RequestBody WalletRequestModel requestModel) throws InvalidAmountException {
        Wallet returnedWallet = walletService.deposit(walletId, requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{wallet_id}/withdraw")
    public ResponseEntity<Wallet> withdraw(@PathVariable("wallet_id") int walletId, @RequestBody WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException {
        Wallet returnedWallet = walletService.withdraw(walletId, requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @GetMapping("/wallets")
    public ResponseEntity<List<WalletResponseModel>> wallets(){
        List<WalletResponseModel> responseWallets = walletService.getAllWallets();

        return new ResponseEntity<>(responseWallets, HttpStatus.OK);
    }
}
