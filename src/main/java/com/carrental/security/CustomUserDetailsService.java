package com.carrental.security;

import com.carrental.entity.User;
import com.carrental.repository.AdminRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    AdminRepository adminRepository,
                                    CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No account with this email"));

        String role = null;
        if (adminRepository.findByUser(user).isPresent()) {
            role = "ADMIN";
        } else if (customerRepository.findByUser(user).isPresent()) {
            role = "CUSTOMER";
        }

        if (role == null) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(new ArrayList<GrantedAuthority>())
                    .disabled(true)
                    .build();
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(role)
                .build();
    }
}