package com.carrental.controller;

import com.carrental.exception.BusinessException;
import com.carrental.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // filter=low shows only reviews with fewer than 3 stars
    @GetMapping
    public String list(@RequestParam(required = false) String filter, Model model) {
        boolean low = "low".equalsIgnoreCase(filter);
        model.addAttribute("reviews", low ? reviewService.findLowRated() : reviewService.findAll());
        model.addAttribute("lowOnly", low);
        return "admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) String filter,
                         RedirectAttributes redirect) {
        try {
            reviewService.delete(id);
            redirect.addFlashAttribute("success", "Review deleted.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "low".equalsIgnoreCase(filter) ? "redirect:/admin/reviews?filter=low" : "redirect:/admin/reviews";
    }
}