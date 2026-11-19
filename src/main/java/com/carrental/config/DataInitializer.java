package com.carrental.config;

import com.carrental.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        boolean created = userService.createAdminIfMissing(adminName, adminEmail, adminPassword);
        if (created) {
            log.info("First admin account created: {}", adminEmail);
        } else {
            log.info("Admin account already exists: {}", adminEmail);
        }
    }
}