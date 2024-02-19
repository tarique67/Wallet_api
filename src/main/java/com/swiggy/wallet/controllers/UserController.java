package com.swiggy.wallet.controllers;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.requestModels.UserRequestModel;
import com.swiggy.wallet.responseModels.ResponseMessageModel;
import com.swiggy.wallet.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("")
    public ResponseEntity<User> registerUser(@RequestBody UserRequestModel user) throws UserAlreadyExistsException {
        User returnedUser = userService.register(user);
        return new ResponseEntity<>(returnedUser, HttpStatus.CREATED);
    }

    @DeleteMapping("")
    public ResponseEntity<ResponseMessageModel> deleteUser() throws UserNotFoundException {
        String response = userService.delete();
        return new ResponseEntity<>(new ResponseMessageModel(response), HttpStatus.ACCEPTED);
    }

}
