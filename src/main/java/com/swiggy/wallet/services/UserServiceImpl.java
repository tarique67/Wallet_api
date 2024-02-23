package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.entities.Wallet;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.UserRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.swiggy.wallet.responseModels.ResponseMessage.*;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDAO userDao;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(UserRequestModel user) throws UserAlreadyExistsException {
        if(userDao.findByUserName(user.getUserName()).isPresent())
            throw new UserAlreadyExistsException(USERNAME_ALREADY_TAKEN);
        User userToSave = new User(user.getUserName(), passwordEncoder.encode(user.getPassword()), user.getCountry());
        return userDao.save(userToSave);
    }

    @Override
    public String delete() throws UserNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userToDelete = userDao.findByUserName(username);
        if(userToDelete.isEmpty())
            throw new UserNotFoundException("User could not be found.");

        userDao.delete(userToDelete.get());
        return USER_DELETED_SUCCESSFULLY;
    }

    @Override
    public User addWallet(int userId) throws UserNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByUserName(username).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        if(user.getUserId() != userId)
            throw new UserNotFoundException(WRONG_USER_ID);

        user.getWallets().add(new Wallet(user.getCountry()));
        return userDao.save(user);
    }

}
