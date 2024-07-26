package com.hudhud.config;

import com.hudhud.model.Client;
import com.hudhud.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Client> user = userRepository.findClientByUsername(username);
        return user.map(UserMapper::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

    }
}
