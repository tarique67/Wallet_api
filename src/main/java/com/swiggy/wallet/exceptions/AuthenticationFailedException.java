package com.swiggy.wallet.exceptions;

public class AuthenticationFailedException extends Exception{

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
