package com.carrental.repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBooking(Booking booking);

    Optional<Review> findByBooking(Booking booking);

    List<Review> findByCustomerOrderByReviewDateDesc(Customer customer);

    List<Review> findByBooking_CarOrderByReviewDateDesc(Car car);

    List<Review> findAllByOrderByReviewDateDesc();
}