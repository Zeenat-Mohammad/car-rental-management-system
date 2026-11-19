//package com.carrental.service;
//
//import com.carrental.entity.Car;
//import com.carrental.entity.Review;
//import com.carrental.enums.CarStatus;
//import com.carrental.exception.BusinessException;
//import com.carrental.repository.CarRepository;
//import com.carrental.repository.ReviewRepository;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Service
//public class CarSearchService {
//
//    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
//
//    private final CarRepository carRepository;
//    private final ReviewRepository reviewRepository;
//
//    public CarSearchService(CarRepository carRepository, ReviewRepository reviewRepository) {
//        this.carRepository = carRepository;
//        this.reviewRepository = reviewRepository;
//    }
//
//    public List<Car> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice) {
//        String key = keyword == null ? "" : keyword.trim().toLowerCase();
//        if (key.length() > 100) {
//            key = key.substring(0, 100);
//        }
//        String cat = (category != null && CarService.CATEGORIES.contains(category)) ? category : "";
//        BigDecimal min = (minPrice == null || minPrice.signum() < 0) ? BigDecimal.ZERO : minPrice;
//        BigDecimal max = (maxPrice == null || maxPrice.compareTo(MAX_PRICE) > 0) ? MAX_PRICE : maxPrice;
//
//        if (min.compareTo(max) > 0) {
//            throw new BusinessException("Minimum price cannot be more than maximum price.");
//        }
//        return carRepository.search(CarStatus.AVAILABLE, key, cat, min, max);
//    }
//
//    public Car getById(Long id) {
//        return carRepository.findById(id)
//                .orElseThrow(() -> new BusinessException("Car not found."));
//    }
//
//    public List<Review> getReviews(Car car) {
//        return reviewRepository.findByBooking_CarOrderByReviewDateDesc(car);
//    }
//
//    public Double averageRating(List<Review> reviews) {
//        if (reviews.isEmpty()) {
//            return null;
//        }
//        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
//    }
//}

package com.carrental.service;

import com.carrental.entity.Car;
import com.carrental.entity.Review;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.exception.BusinessException;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

// Used by the public car pages
@Service
public class CarSearchService {

    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final CarRepository carRepository;
    private final ReviewRepository reviewRepository;

    public CarSearchService(CarRepository carRepository, ReviewRepository reviewRepository) {
        this.carRepository = carRepository;
        this.reviewRepository = reviewRepository;
    }

    // Only AVAILABLE cars are listed. Every filter is optional. (Used by the home page.)
    public List<Car> search(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        if (key.length() > 100) {
            key = key.substring(0, 100);
        }
        String cat = (category != null && CarService.CATEGORIES.contains(category)) ? category : "";
        BigDecimal min = (minPrice == null || minPrice.signum() < 0) ? BigDecimal.ZERO : minPrice;
        BigDecimal max = (maxPrice == null || maxPrice.compareTo(MAX_PRICE) > 0) ? MAX_PRICE : maxPrice;

        if (min.compareTo(max) > 0) {
            throw new BusinessException("Minimum price cannot be more than maximum price.");
        }
        return carRepository.search(CarStatus.AVAILABLE, key, cat, min, max);
    }

    // The full catalogue: ALL cars (cars under maintenance are shown as unavailable).
    // sort: "price_asc" (default), "price_desc" or "newest". Available cars always come first.
    public List<Car> catalogue(String keyword, String category, String fuel, String transmission,
                               Integer minSeats, BigDecimal minPrice, BigDecimal maxPrice, String sort) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        if (key.length() > 100) {
            key = key.substring(0, 100);
        }
        final String k = key;
        final String cat = (category != null && CarService.CATEGORIES.contains(category)) ? category : "";
        final FuelType fuelType = parseFuel(fuel);
        final Transmission trans = parseTransmission(transmission);
        final int seats = minSeats == null ? 0 : minSeats;
        final BigDecimal min = (minPrice == null || minPrice.signum() < 0) ? BigDecimal.ZERO : minPrice;
        final BigDecimal max = (maxPrice == null || maxPrice.compareTo(MAX_PRICE) > 0) ? MAX_PRICE : maxPrice;

        if (min.compareTo(max) > 0) {
            throw new BusinessException("Minimum price cannot be more than maximum price.");
        }

        Comparator<Car> byPrice = Comparator.comparing(Car::getRate);
        Comparator<Car> order;
        if ("price_desc".equals(sort)) {
            order = byPrice.reversed();
        } else if ("newest".equals(sort)) {
            order = Comparator.comparing(Car::getCarId).reversed();
        } else {
            order = byPrice;
        }
        Comparator<Car> availableFirst = Comparator.comparing(c -> c.getStatus() != CarStatus.AVAILABLE);

        return carRepository.findAll().stream()
                .filter(c -> k.isEmpty()
                        || (c.getBrand() + " " + c.getModel()).toLowerCase().contains(k))
                .filter(c -> cat.isEmpty() || c.getCategory().equals(cat))
                .filter(c -> fuelType == null || c.getFuelType() == fuelType)
                .filter(c -> trans == null || c.getTransmission() == trans)
                .filter(c -> c.getTotalSeat() >= seats)
                .filter(c -> c.getRate().compareTo(min) >= 0 && c.getRate().compareTo(max) <= 0)
                .sorted(availableFirst.thenComparing(order))
                .toList();
    }

    public Car getById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Car not found."));
    }

    public List<Review> getReviews(Car car) {
        return reviewRepository.findByBooking_CarOrderByReviewDateDesc(car);
    }

    // null when there are no reviews yet
    public Double averageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    private FuelType parseFuel(String value) {
        try {
            return (value == null || value.isBlank()) ? null : FuelType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Transmission parseTransmission(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Transmission.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}