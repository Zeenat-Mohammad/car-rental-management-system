package com.carrental.repository;

import com.carrental.entity.Admin;
import com.carrental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUser(User user);
}