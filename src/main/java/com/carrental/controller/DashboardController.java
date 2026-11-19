package com.carrental.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // After login everyone lands here: admins go to their dashboard, customers go to the home page
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin/dashboard" : "redirect:/";
    }

    // The customer dashboard was removed. Old links go to the home page.
    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "redirect:/";
    }

    // /admin/dashboard is in AdminDashboardController
}