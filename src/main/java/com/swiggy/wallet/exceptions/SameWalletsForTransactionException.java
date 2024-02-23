package com.swiggy.wallet.exceptions;

public class SameWalletsForTransactionException extends Exception{

    public SameWalletsForTransactionException(String message) {
        super(message);
    }
}
