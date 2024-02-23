package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.requestModels.UserRequestModel;

public interface UserService {

    User register(UserRequestModel user) throws UserAlreadyExistsException;

    String delete() throws UserNotFoundException;

    User addWallet(int userId) throws UserNotFoundException;
}
