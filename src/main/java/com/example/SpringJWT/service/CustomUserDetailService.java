package com.example.SpringJWT.service;

import com.example.SpringJWT.dto.CustomUserDetails;
import com.example.SpringJWT.entity.UserEntity;
import com.example.SpringJWT.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository){

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("CustomUserDetailService.loadUserByUsername");

        UserEntity userData = userRepository.findByUsername(username);

        if (userData == null) {
            System.out.println("By ID, userData  is null");
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        System.out.println("By ID, userData is not null");
        return new CustomUserDetails(userData);
    }
}
