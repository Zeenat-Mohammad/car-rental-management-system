package com.carrental.service;

import com.carrental.dto.AvailabilityResult;
import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.CarStatus;
import com.carrental.enums.PaymentStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.AdditionalChargeRepository;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.MaintenanceRepository;
import com.carrental.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private static final long SECONDS_PER_DAY = 24L * 60 * 60;

    private final BookingRepository bookingRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AdditionalChargeRepository additionalChargeRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository,
                          MaintenanceRepository maintenanceRepository,
                          AdditionalChargeRepository additionalChargeRepository,
                          PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.additionalChargeRepository = additionalChargeRepository;
        this.paymentRepository = paymentRepository;
    }

    public int calculateDays(LocalDateTime pickup, LocalDateTime returnTime) {
        long seconds = Duration.between(pickup, returnTime).getSeconds();
        long days = (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
        return (int) Math.max(1, days);
    }

    public BigDecimal calculateRentalCost(Car car, int days) {
        return car.getRate().multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    public AvailabilityResult check(Car car, LocalDateTime pickup, LocalDateTime returnTime) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        if (pickup.isBefore(now)) {
            return AvailabilityResult.notAvailable("Pickup date and time cannot be in the past.");
        }
        if (!returnTime.isAfter(pickup)) {
            return AvailabilityResult.notAvailable("Return date and time must be after the pickup date and time.");
        }
        if (car.getStatus() != CarStatus.AVAILABLE) {
            return AvailabilityResult.notAvailable("This car is currently under maintenance and cannot be booked.");
        }
        if (maintenanceRepository.hasOngoingOverlap(car.getCarId(), pickup, returnTime)) {
            return AvailabilityResult.notAvailable(
                    "Car not available for selected dates (under maintenance). Please choose other dates.");
        }
        if (bookingRepository.hasConfirmedOverlap(car.getCarId(), pickup, returnTime)) {
            return AvailabilityResult.notAvailable(
                    "Car not available for selected dates (already booked). Please choose other dates.");
        }
        int days = calculateDays(pickup, returnTime);
        return AvailabilityResult.ok(days, calculateRentalCost(car, days));
    }

    @Transactional
    public Booking createPendingBooking(Customer customer, Car car, LocalDateTime pickup, LocalDateTime returnTime) {
        AvailabilityResult result = check(car, pickup, returnTime);
        if (!result.isAvailable()) {
            throw new BusinessException(result.getMessage());
        }
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCar(car);
        booking.setPickupDateTime(pickup);
        booking.setReturnDateTime(returnTime);
        booking.setTotalDays(result.getTotalDays());
        booking.setTotalCost(result.getRentalCost());
        booking.setStatus(BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    public List<Booking> findByCustomer(Customer customer) {
        return bookingRepository.findByCustomerOrderByPickupDateTimeDesc(customer);
    }

    public Booking getOwnBooking(Long bookingId, Customer customer) {
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                .orElseThrow(() -> new BusinessException("Booking not found."));
    }

    public List<AdditionalCharge> getCharges(Booking booking) {
        return additionalChargeRepository.findByBooking(booking);
    }

    public BigDecimal sumCharges(List<AdditionalCharge> charges) {
        BigDecimal total = BigDecimal.ZERO;
        for (AdditionalCharge charge : charges) {
            total = total.add(charge.getAmount());
        }
        return total;
    }

    public BigDecimal dailyRate(Booking booking) {
        return booking.getTotalCost().divide(BigDecimal.valueOf(booking.getTotalDays()), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void cancelPendingBooking(Long bookingId, Customer customer) {
        Booking booking = getOwnBooking(bookingId, customer);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only a pending booking can be cancelled here.");
        }
        for (Payment payment : paymentRepository.findByBookingOrderByPaymentDateDesc(booking)) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}