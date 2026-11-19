package com.carrental.controller;

import com.carrental.service.CarService;
import com.carrental.service.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("cars", homeService.featuredCars());
        model.addAttribute("categories", CarService.CATEGORIES);
        model.addAttribute("availableCars", homeService.availableCarCount());
        model.addAttribute("customerCount", homeService.customerCount());
        model.addAttribute("completedRentals", homeService.completedRentals());
        model.addAttribute("happyReviews", homeService.happyReviews());
        model.addAttribute("reviewCount", homeService.reviewCount());
        model.addAttribute("averageRating", homeService.averageRating());
        return "index";
    }
}