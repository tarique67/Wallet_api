package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.requestModels.UserRequestModel;

public interface UserService {

    User register(UserRequestModel user) throws UserAlreadyExistsException;

    String delete() throws UserNotFoundException;

}
