//package com.carrental.controller;
//
//import com.carrental.entity.Car;
//import com.carrental.entity.Review;
//import com.carrental.exception.BusinessException;
//import com.carrental.service.CarSearchService;
//import com.carrental.service.CarService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Controller
//@RequestMapping("/cars")
//public class CarController {
//
//    private final CarSearchService carSearchService;
//
//    public CarController(CarSearchService carSearchService) {
//        this.carSearchService = carSearchService;
//    }
//
//    @GetMapping
//    public String list(@RequestParam(required = false) String keyword,
//                       @RequestParam(required = false) String category,
//                       @RequestParam(required = false) BigDecimal minPrice,
//                       @RequestParam(required = false) BigDecimal maxPrice,
//                       Model model) {
//        List<Car> cars;
//        try {
//            cars = carSearchService.search(keyword, category, minPrice, maxPrice);
//        } catch (BusinessException e) {
//            cars = List.of();
//            model.addAttribute("filterError", e.getMessage());
//        }
//        model.addAttribute("cars", cars);
//        model.addAttribute("keyword", keyword);
//        model.addAttribute("category", category);
//        model.addAttribute("minPrice", minPrice);
//        model.addAttribute("maxPrice", maxPrice);
//        model.addAttribute("categories", CarService.CATEGORIES);
//        return "cars";
//    }
//
//    @GetMapping("/{id}")
//    public String details(@PathVariable Long id, Model model, RedirectAttributes redirect) {
//        Car car;
//        try {
//            car = carSearchService.getById(id);
//        } catch (BusinessException e) {
//            redirect.addFlashAttribute("error", e.getMessage());
//            return "redirect:/cars";
//        }
//        List<Review> reviews = carSearchService.getReviews(car);
//        model.addAttribute("car", car);
//        model.addAttribute("reviews", reviews);
//        model.addAttribute("averageRating", carSearchService.averageRating(reviews));
//        return "car-details";
//    }
//}

package com.carrental.controller;

import com.carrental.entity.Car;
import com.carrental.entity.Review;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.exception.BusinessException;
import com.carrental.service.CarGalleryService;
import com.carrental.service.CarSearchService;
import com.carrental.service.CarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarSearchService carSearchService;
    private final CarGalleryService carGalleryService;

    public CarController(CarSearchService carSearchService, CarGalleryService carGalleryService) {
        this.carSearchService = carSearchService;
        this.carGalleryService = carGalleryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String fuel,
                       @RequestParam(required = false) String transmission,
                       @RequestParam(required = false) Integer minSeats,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sort,
                       Model model) {
        List<Car> cars;
        try {
            cars = carSearchService.catalogue(keyword, category, fuel, transmission,
                    minSeats, minPrice, maxPrice, sort);
        } catch (BusinessException e) {
            cars = List.of();
            model.addAttribute("filterError", e.getMessage());
        }
        model.addAttribute("cars", cars);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("fuel", fuel);
        model.addAttribute("transmission", transmission);
        model.addAttribute("minSeats", minSeats);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", CarService.CATEGORIES);
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        return "cars";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Car car;
        try {
            car = carSearchService.getById(id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars";
        }
        List<Review> reviews = carSearchService.getReviews(car);
        model.addAttribute("car", car);
        model.addAttribute("photos", carGalleryService.photoUrls(car));
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", carSearchService.averageRating(reviews));
        return "car-details";
    }
}