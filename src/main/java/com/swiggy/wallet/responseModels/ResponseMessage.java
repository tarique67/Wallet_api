package com.swiggy.wallet.responseModels;

public class ResponseMessage {

    public static final String USER_NOT_FOUND = "User not found.";
    public static final String USERNAME_ALREADY_TAKEN = "Username taken. Please try with another username.";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully.";
    public static final String TRANSACTION_SUCCESSFUL = "InterWalletTransaction successful";
    public static final String DEPOSIT_SHOULD_BE_GREATER_THAN_0 = "Amount to deposit should be greater than 0.";
    public static final String INSUFFICIENT_BALANCE_EXCEPTION = "Insufficient balance.";
    public static final String WITHDRAWAL_SHOULD_BE_GREATER_THAN_0 = "Withdrawal amount should be greater than 0.";
    public static final String WALLET_ID_DOES_NOT_MATCH = "Wrong wallet id provided.";
    public static final String WRONG_USER_ID = "Wrong user id provided.";
    public static final String ACCEPTED_LOCATIONS = "Only INDIA USA EUROPE locations and their currencies accepted.";
    public static final String SENDER_WALLET_NOT_FOUND = "Sender wallet not found.";
    public static final String RECEIVER_WALLET_NOT_FOUND = "Receiver wallet not found.";
    public static final String WALLETS_SAME_IN_TRANSACTION = "Sender and receiver wallets cannot be same for a transaction.";
    public static final String AMOUNT_LESS_THAN_SERVICE_CHARGE = "Amount less than service charge.";
}
