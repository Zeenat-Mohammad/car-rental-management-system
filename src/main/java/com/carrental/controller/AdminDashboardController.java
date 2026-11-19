package com.carrental.controller;

import com.carrental.entity.Review;
import com.carrental.service.AdminStatsService;
import com.carrental.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminDashboardController {

    // How many low-rated reviews fit in one row on the dashboard
    private static final int LOW_REVIEWS_SHOWN = 3;

    private final AdminStatsService adminStatsService;
    private final ReviewService reviewService;

    public AdminDashboardController(AdminStatsService adminStatsService, ReviewService reviewService) {
        this.adminStatsService = adminStatsService;
        this.reviewService = reviewService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Review> low = reviewService.findLowRated();
        model.addAttribute("stats", adminStatsService.getStats());
        model.addAttribute("lowReviews", low.stream().limit(LOW_REVIEWS_SHOWN).toList());
        model.addAttribute("lowReviewCount", low.size());
        return "admin/dashboard";
    }
}