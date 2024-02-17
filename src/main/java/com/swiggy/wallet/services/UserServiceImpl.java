package com.swiggy.wallet.services;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.exceptions.InsufficientBalanceException;
import com.swiggy.wallet.exceptions.InvalidAmountException;
import com.swiggy.wallet.exceptions.UserAlreadyExistsException;
import com.swiggy.wallet.exceptions.UserNotFoundException;
import com.swiggy.wallet.repository.UserDAO;
import com.swiggy.wallet.requestModels.TransactionRequestModel;
import com.swiggy.wallet.requestModels.UserRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            throw new UserAlreadyExistsException("Username taken. Please try with another username.");
        User userToSave = new User(user.getUserName(), passwordEncoder.encode(user.getPassword()));
        return userDao.save(userToSave);
    }

    @Override
    public String delete() throws UserNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userToDelete = userDao.findByUserName(username);
        if(userToDelete.isEmpty())
            throw new UserNotFoundException("User could not be found.");

        userDao.delete(userToDelete.get());
        return "User " + username + " deleted successfully.";
    }

    @Override
    public String transact(TransactionRequestModel requestModel) throws InsufficientBalanceException, InvalidAmountException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userDao.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User "+ username + " not found."));
        User receiver = userDao.findByUserName(requestModel.getReceiverName()).orElseThrow(() -> new UsernameNotFoundException("User "+ requestModel.getReceiverName() + " not found."));

        walletService.transact(sender.getWallet(), receiver.getWallet(), requestModel.getMoney());

        userDao.save(sender);
        userDao.save(receiver);

        return "Transaction Successful.";
    }
}
