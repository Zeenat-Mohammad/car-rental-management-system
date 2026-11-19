package com.carrental.service;

import com.carrental.dto.RegistrationForm;
import com.carrental.entity.Admin;
import com.carrental.entity.Customer;
import com.carrental.entity.User;
import com.carrental.repository.AdminRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    public boolean licenseExists(String licenseNumber) {
        return customerRepository.existsByLicenseNumber(licenseNumber.trim());
    }

    @Transactional
    public Customer registerCustomer(RegistrationForm form) {
        User user = new User();
        user.setUserName(form.getUserName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPhoneNumber(form.getPhoneNumber().trim());
        customer.setAddress(form.getAddress().trim());
        customer.setLicenseNumber(form.getLicenseNumber().trim());
        return customerRepository.save(customer);
    }

    @Transactional
    public boolean createAdminIfMissing(String name, String email, String rawPassword) {
        String normalized = normalizeEmail(email);
        if (userRepository.existsByEmail(normalized)) {
            return false;
        }
        User user = new User();
        user.setUserName(name);
        user.setEmail(normalized);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        Admin admin = new Admin();
        admin.setUser(user);
        adminRepository.save(admin);
        return true;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}