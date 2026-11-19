package com.carrental.controller;

import com.carrental.dto.ReviewForm;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.exception.BusinessException;
import com.carrental.service.CustomerService;
import com.carrental.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/bookings/{id}/review")
public class CustomerReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;

    public CustomerReviewController(ReviewService reviewService, CustomerService customerService) {
        this.reviewService = reviewService;
        this.customerService = customerService;
    }

    // Shows the review if there is one, otherwise the form
    @GetMapping
    public String show(@PathVariable Long id,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            Booking booking = reviewService.getReviewableBooking(id, customer);
            model.addAttribute("booking", booking);
            model.addAttribute("review", reviewService.findByBooking(booking).orElse(null));
            model.addAttribute("reviewForm", new ReviewForm());
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings/" + id;
        }
        return "customer/review";
    }

    @PostMapping
    public String submit(@PathVariable Long id,
                         @Valid @ModelAttribute("reviewForm") ReviewForm form,
                         BindingResult result,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        Booking booking;
        try {
            booking = reviewService.getReviewableBooking(id, customer);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings/" + id;
        }
        if (result.hasErrors()) {
            model.addAttribute("booking", booking);
            model.addAttribute("review", null);
            return "customer/review";
        }
        try {
            reviewService.submit(id, customer, form);
            redirect.addFlashAttribute("success", "Thank you! Your review has been saved.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id + "/review";
    }
}