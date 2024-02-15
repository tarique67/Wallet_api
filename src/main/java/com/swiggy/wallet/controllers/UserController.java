package com.swiggy.wallet.controllers;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.requestModels.UserRequestModel;
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

}
