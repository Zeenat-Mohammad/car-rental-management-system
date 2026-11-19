package com.carrental.service;

import com.carrental.entity.Customer;
import com.carrental.entity.User;
import com.carrental.exception.BusinessException;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CustomerService(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public Customer getByEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Account not found."));
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Customer profile not found."));
    }
}