package com.carrental.service;

import com.carrental.entity.Car;
import com.carrental.entity.Review;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.CarStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Everything the public home page needs
@Service
public class HomeService {

    private static final int FEATURED_CARS = 6;
    private static final int HAPPY_REVIEWS_SHOWN = 3;
    // "Happy customer" = a review with this many stars or more
    public static final int HAPPY_RATING = 4;

    private final CarSearchService carSearchService;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public HomeService(CarSearchService carSearchService,
                       CarRepository carRepository,
                       CustomerRepository customerRepository,
                       BookingRepository bookingRepository,
                       ReviewRepository reviewRepository) {
        this.carSearchService = carSearchService;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    // The first available cars, cheapest first
    public List<Car> featuredCars() {
        return carSearchService.search(null, null, null, null).stream().limit(FEATURED_CARS).toList();
    }

    public long availableCarCount() {
        return carRepository.findByStatus(CarStatus.AVAILABLE).size();
    }

    public long customerCount() {
        return customerRepository.count();
    }

    public long completedRentals() {
        return bookingRepository.countByStatus(BookingStatus.COMPLETED);
    }

    // Latest reviews with 4 stars or more that have a written comment
    public List<Review> happyReviews() {
        return reviewRepository.findAllByOrderByReviewDateDesc().stream()
                .filter(r -> r.getRating() >= HAPPY_RATING)
                .filter(r -> r.getComment() != null && !r.getComment().isBlank())
                .limit(HAPPY_REVIEWS_SHOWN)
                .toList();
    }

    public long reviewCount() {
        return reviewRepository.count();
    }

    // null when there are no reviews yet
    public Double averageRating() {
        List<Review> all = reviewRepository.findAll();
        if (all.isEmpty()) {
            return null;
        }
        return all.stream().mapToInt(Review::getRating).average().orElse(0);
    }
}