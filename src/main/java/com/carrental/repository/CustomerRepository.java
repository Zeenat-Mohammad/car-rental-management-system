package com.carrental.repository;

import com.carrental.entity.Customer;
import com.carrental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser(User user);

    boolean existsByLicenseNumber(String licenseNumber);
}