package com.carrental.service;

import com.carrental.dto.ReviewForm;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.entity.Review;
import com.carrental.enums.BookingStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    // A review with fewer stars than this is a "low" review
    public static final int LOW_RATING_LIMIT = 3;

    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;

    public ReviewService(ReviewRepository reviewRepository, BookingService bookingService) {
        this.reviewRepository = reviewRepository;
        this.bookingService = bookingService;
    }

    // The customer's own booking, and only if the rental is completed (BR6)
    public Booking getReviewableBooking(Long bookingId, Customer customer) {
        Booking booking = bookingService.getOwnBooking(bookingId, customer);
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("You can rate a rental only after it is completed.");
        }
        return booking;
    }

    public Optional<Review> findByBooking(Booking booking) {
        return reviewRepository.findByBooking(booking);
    }

    // One review per booking
    @Transactional
    public Review submit(Long bookingId, Customer customer, ReviewForm form) {
        Booking booking = getReviewableBooking(bookingId, customer);
        if (reviewRepository.existsByBooking(booking)) {
            throw new BusinessException("You have already reviewed this booking.");
        }
        Review review = new Review();
        review.setCustomer(customer);
        review.setBooking(booking);
        review.setRating(form.getRating());
        String comment = form.getComment() == null ? "" : form.getComment().trim();
        review.setComment(comment.isEmpty() ? null : comment);
        review.setReviewDate(LocalDate.now());
        return reviewRepository.save(review);
    }

    // ---------- admin ----------
    public List<Review> findAll() {
        return reviewRepository.findAllByOrderByReviewDateDesc();
    }

    // Reviews with fewer than 3 stars, newest first
    public List<Review> findLowRated() {
        return reviewRepository.findAllByOrderByReviewDateDesc().stream()
                .filter(r -> r.getRating() < LOW_RATING_LIMIT)
                .toList();
    }

    // The admin removes a review (moderation)
    @Transactional
    public void delete(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found."));
        reviewRepository.delete(review);
    }
}