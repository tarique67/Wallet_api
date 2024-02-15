package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.UserRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDAO userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerCustomer(UserRequestModel user) throws UserAlreadyExistsException {
        if(userDao.findByUserName(user.getUserName()).isPresent())
            throw new UserAlreadyExistsException("Username taken. Please try with another username.");
        User userToSave = new User(user.getUserName(), passwordEncoder.encode(user.getPassword()), new Wallet());
        return userDao.save(userToSave);
    }
}
