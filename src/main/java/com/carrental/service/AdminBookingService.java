package com.carrental.service;

import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import com.carrental.entity.Payment;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.AdditionalChargeRepository;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminBookingService {

    private static final long GRACE_HOURS = 2;
    private static final long SECONDS_PER_DAY = 24L * 60 * 60;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final AdditionalChargeRepository additionalChargeRepository;
    private final BookingService bookingService;

    public AdminBookingService(BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               AdditionalChargeRepository additionalChargeRepository,
                               BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.additionalChargeRepository = additionalChargeRepository;
        this.bookingService = bookingService;
    }

    public List<Booking> findAll(String status) {
        List<Booking> all = bookingRepository.findAllByOrderByPickupDateTimeDesc();
        if (status == null || status.isBlank()) {
            return all;
        }
        BookingStatus wanted;
        try {
            wanted = BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return all;
        }
        return all.stream().filter(b -> b.getStatus() == wanted).toList();
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found."));
    }

    @Transactional
    public Long recordCash(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found."));
        if (payment.getMethod() != PaymentMethod.PAY_AT_PICKUP || payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("This payment is not waiting for cash at pickup.");
        }
        if (payment.getBooking().getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Cash can only be recorded for a confirmed booking.");
        }
        payment.setStatus(PaymentStatus.SUCCESSFUL);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        return payment.getBooking().getBookingId();
    }

    public int lateDays(Booking booking, LocalDateTime actualReturn) {
        long lateSeconds = Duration.between(booking.getReturnDateTime(), actualReturn).getSeconds()
                - GRACE_HOURS * 3600;
        if (lateSeconds <= 0) {
            return 0;
        }
        return (int) ((lateSeconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY);
    }

    @Transactional
    public String completeBooking(Long bookingId, LocalDateTime actualReturn) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only a confirmed booking can be completed.");
        }
        if (actualReturn == null) {
            throw new BusinessException("Enter the actual return date and time.");
        }
        if (!actualReturn.isAfter(booking.getPickupDateTime())) {
            throw new BusinessException("The actual return must be after the pickup date and time.");
        }

        int days = lateDays(booking, actualReturn);
        String message = "Booking completed. Returned on time (or within the 2-hour grace period): no late charge.";

        if (days > 0) {
            BigDecimal charge = bookingService.dailyRate(booking).multiply(BigDecimal.valueOf(days));
            AdditionalCharge extra = new AdditionalCharge();
            extra.setBooking(booking);
            extra.setAmount(charge);
            extra.setDescription("Late return: " + days + " extra day" + (days == 1 ? "" : "s"));
            additionalChargeRepository.save(extra);
            message = "Booking completed. Late return charge added: Rs. " + charge
                    + " (" + days + " extra day" + (days == 1 ? "" : "s") + ").";
        }

        booking.setActualReturnDateTime(actualReturn);
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        return message;
    }
}