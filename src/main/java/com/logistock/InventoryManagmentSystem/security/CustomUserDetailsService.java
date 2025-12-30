package com.logistock.InventoryManagmentSystem.security;

import com.logistock.InventoryManagmentSystem.exceptions.NotFoundException;
import com.logistock.InventoryManagmentSystem.models.User;
import com.logistock.InventoryManagmentSystem.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User email not found."));

        return AuthUser.builder()
                .user(user)
                .build();
    }
}
