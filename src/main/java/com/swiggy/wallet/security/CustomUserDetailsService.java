package com.swiggy.wallet.security;

import com.swiggy.wallet.entities.User;
import com.swiggy.wallet.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDAO userDao;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userDao.findByUserName(username).orElseThrow(()-> new UsernameNotFoundException("No user found with name "+ username));
        return new CustomUserDetails(user);
    }
}
