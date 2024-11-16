package com.mmt.btl.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mmt.btl.entity.User;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.repository.UserRepository;

public class UserSecurityService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new MMTNotFoundException());
        return UserSecurity.build(user);
    }

}