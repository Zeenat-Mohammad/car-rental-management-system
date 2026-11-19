package com.carrental.controller;

import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.exception.BusinessException;
import com.carrental.service.AdminCustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("rows", adminCustomerService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/customers";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Customer customer;
        try {
            customer = adminCustomerService.getById(id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/customers";
        }
        List<Booking> bookings = adminCustomerService.getBookings(customer);
        model.addAttribute("customer", customer);
        model.addAttribute("bookings", bookings);
        return "admin/customer-details";
    }
}