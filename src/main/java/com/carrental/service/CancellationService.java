package com.carrental.service;

import com.carrental.dto.CancellationQuote;
import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import com.carrental.entity.Refund;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.PaymentStatus;
import com.carrental.enums.RefundStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.AdditionalChargeRepository;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CancellationService {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final AdditionalChargeRepository additionalChargeRepository;

    public CancellationService(BookingService bookingService,
                               PaymentService paymentService,
                               BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               RefundRepository refundRepository,
                               AdditionalChargeRepository additionalChargeRepository) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.additionalChargeRepository = additionalChargeRepository;
    }

    public CancellationQuote quote(Booking booking) {
        Duration remaining = Duration.between(LocalDateTime.now(), booking.getPickupDateTime());

        int percent;
        String rule;
        if (remaining.compareTo(Duration.ofHours(48)) > 0) {
            percent = 0;
            rule = "More than 48 hours before pickup: no cancellation charge";
        } else if (remaining.compareTo(Duration.ofHours(24)) >= 0) {
            percent = 10;
            rule = "24 to 48 hours before pickup: 10% of the rental amount";
        } else {
            percent = 20;
            rule = "Less than 24 hours before pickup: 20% of the rental amount";
        }

        BigDecimal rental = booking.getTotalCost();
        BigDecimal calculated = rental.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal paid = paymentService.amountPaid(booking);
        BigDecimal applied = calculated.min(paid);
        BigDecimal refund = paid.subtract(applied);

        return new CancellationQuote(percent, rule, rental, calculated, paid, applied, refund);
    }

    @Transactional
    public String cancel(Long bookingId, Customer customer) {
        Booking booking = bookingService.getOwnBooking(bookingId, customer);
        return cancelBooking(booking);
    }

    @Transactional
    public String cancelByAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found."));
        return cancelBooking(booking);
    }

    private String cancelBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) {
            bookingService.cancelPendingBooking(booking.getBookingId(), booking.getCustomer());
            return "Booking cancelled. No cancellation charge applies to a pending booking.";
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("This booking cannot be cancelled.");
        }

        CancellationQuote quote = quote(booking);

        if (quote.getChargeApplied().signum() > 0) {
            AdditionalCharge charge = new AdditionalCharge();
            charge.setBooking(booking);
            charge.setAmount(quote.getChargeApplied());
            charge.setDescription("Cancellation charge (" + quote.getPercent() + "% of rental amount)");
            additionalChargeRepository.save(charge);
        }

        BigDecimal remaining = quote.getRefundAmount();
        for (Payment payment : paymentService.getPayments(booking)) {
            if (remaining.signum() <= 0) {
                break;
            }
            if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
                continue;
            }
            BigDecimal portion = remaining.min(payment.getAmount());
            refundRepository.save(new Refund(payment, portion, RefundStatus.PROCESSED));
            if (portion.compareTo(payment.getAmount()) == 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
            remaining = remaining.subtract(portion);
        }

        for (Payment payment : paymentService.getPayments(booking)) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return "Booking cancelled. Cancellation charge: Rs. " + quote.getChargeApplied()
                + ". Refund: Rs. " + quote.getRefundAmount()
                + (quote.getRefundAmount().signum() > 0 ? " (simulated, no real money moves)." : ".");
    }
}