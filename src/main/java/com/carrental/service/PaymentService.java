package com.carrental.service;

import com.carrental.dto.AvailabilityResult;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import com.carrental.entity.Refund;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.enums.PaymentType;
import com.carrental.enums.RefundStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final BigDecimal ADVANCE_RATE = new BigDecimal("0.30");
    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public PaymentService(BookingService bookingService,
                          BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          RefundRepository refundRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    public BigDecimal advanceAmount(Booking booking) {
        return booking.getTotalCost().multiply(ADVANCE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal balanceAmount(Booking booking) {
        return booking.getTotalCost().subtract(advanceAmount(booking));
    }

    public List<Payment> getPayments(Booking booking) {
        return paymentRepository.findByBookingOrderByPaymentDateDesc(booking);
    }

    public List<Refund> getRefunds(Booking booking) {
        return refundRepository.findByBooking(booking);
    }

    public BigDecimal amountPaid(Booking booking) {
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : getPayments(booking)) {
            if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                total = total.add(payment.getAmount());
            }
        }
        return total;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void payAtPickup(Long bookingId, Customer customer) {
        Booking booking = requirePendingOwnBooking(bookingId, customer);
        ensureStillAvailable(booking);
        abandonOpenOnlinePayments(booking);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        newPayment(booking, booking.getTotalCost(), PaymentMethod.PAY_AT_PICKUP, PaymentType.FULL, "PKP");
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public Payment startOnlinePayment(Long bookingId, Customer customer, String option, PaymentMethod method) {
        Booking booking = requirePendingOwnBooking(bookingId, customer);

        if (method != PaymentMethod.CARD && method != PaymentMethod.UPI && method != PaymentMethod.NET_BANKING) {
            throw new BusinessException("Choose Card, UPI or Net banking for online payment.");
        }
        PaymentType type;
        BigDecimal amount;
        if ("ADVANCE".equals(option)) {
            type = PaymentType.ADVANCE;
            amount = advanceAmount(booking);
        } else if ("FULL".equals(option)) {
            type = PaymentType.FULL;
            amount = booking.getTotalCost();
        } else {
            throw new BusinessException("Choose a payment option.");
        }

        ensureStillAvailable(booking);
        abandonOpenOnlinePayments(booking);
        return newPayment(booking, amount, method, type, "SIM");
    }

    public Payment getOpenOnlinePayment(Long paymentId, Customer customer) {
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getBooking().getCustomer().getCustomerId().equals(customer.getCustomerId()))
                .orElseThrow(() -> new BusinessException("Payment not found."));
        if (payment.getStatus() != PaymentStatus.PENDING
                || payment.getMethod() == PaymentMethod.PAY_AT_PICKUP
                || payment.getBooking().getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("This payment is no longer open.");
        }
        return payment;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void completeSimulatedPayment(Long paymentId, Customer customer, boolean success) {
        Payment payment = getOpenOnlinePayment(paymentId, customer);
        Booking booking = payment.getBooking();

        if (!success) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BusinessException(
                    "Payment failed. You can try again, choose another method, or cancel the booking.");
        }

        AvailabilityResult result = bookingService.check(
                booking.getCar(), booking.getPickupDateTime(), booking.getReturnDateTime());
        if (!result.isAvailable()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            refundRepository.save(new Refund(payment, payment.getAmount(), RefundStatus.PROCESSED));
            cancelUnconfirmed(booking);
            throw new BusinessException("Your payment was received, but the car is no longer available for these dates. "
                    + "The booking was cancelled and the full amount of Rs. " + payment.getAmount()
                    + " was refunded (simulated).");
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        paymentRepository.save(payment);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        if (payment.getPaymentType() == PaymentType.ADVANCE) {
            newPayment(booking, balanceAmount(booking), PaymentMethod.PAY_AT_PICKUP, PaymentType.BALANCE, "PKP");
        }
    }

    private Booking requirePendingOwnBooking(Long bookingId, Customer customer) {
        Booking booking = bookingService.getOwnBooking(bookingId, customer);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("This booking is already confirmed or closed.");
        }
        return booking;
    }

    private void ensureStillAvailable(Booking booking) {
        AvailabilityResult result = bookingService.check(
                booking.getCar(), booking.getPickupDateTime(), booking.getReturnDateTime());
        if (!result.isAvailable()) {
            cancelUnconfirmed(booking);
            throw new BusinessException("The booking was cancelled. " + result.getMessage());
        }
    }

    private void cancelUnconfirmed(Booking booking) {
        for (Payment payment : getPayments(booking)) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void abandonOpenOnlinePayments(Booking booking) {
        for (Payment payment : getPayments(booking)) {
            if (payment.getStatus() == PaymentStatus.PENDING && payment.getMethod() != PaymentMethod.PAY_AT_PICKUP) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }
    }

    private Payment newPayment(Booking booking, BigDecimal amount, PaymentMethod method,
                               PaymentType type, String prefix) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaymentType(type);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId(prefix));
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private String generateTransactionId(String prefix) {
        String id;
        do {
            id = prefix + "-" + LocalDate.now().format(DATE_PART) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (paymentRepository.existsByTransactionId(id));
        return id;
    }
}