package com.swiggy.wallet.controllers;

import com.swiggy.wallet.exceptions.AuthenticationFailedException;
import com.swiggy.wallet.exceptions.WalletNotFoundException;
import com.swiggy.wallet.requestModels.WalletRequestModel;
import com.swiggy.wallet.responseModels.WalletResponseModel;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PutMapping("/{wallet_id}/deposit")
    public ResponseEntity<WalletResponseModel> deposit(@PathVariable("wallet_id") int walletId, @RequestBody WalletRequestModel requestModel) throws InvalidAmountException, AuthenticationFailedException, WalletNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        WalletResponseModel returnedWallet = walletService.deposit(walletId, username, requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{wallet_id}/withdrawal")
    public ResponseEntity<WalletResponseModel> withdraw(@PathVariable("wallet_id") int walletId,@RequestBody WalletRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException, AuthenticationFailedException, WalletNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        WalletResponseModel returnedWallet = walletService.withdraw(walletId, username, requestModel);

        return new ResponseEntity<>(returnedWallet, HttpStatus.ACCEPTED);
    }

    @GetMapping("")
    public ResponseEntity<List<WalletResponseModel>> wallets(){
        List<WalletResponseModel> responseWallets = walletService.getAllWallets();

        return new ResponseEntity<>(responseWallets, HttpStatus.OK);
    }
}
