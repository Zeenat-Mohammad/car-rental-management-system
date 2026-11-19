package com.carrental.service;

import com.carrental.dto.DashboardStats;
import com.carrental.dto.PendingPaymentRow;
import com.carrental.entity.Payment;
import com.carrental.entity.Refund;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.CarStatus;
import com.carrental.enums.PaymentStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RefundRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminStatsService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public AdminStatsService(CarRepository carRepository,
                             BookingRepository bookingRepository,
                             CustomerRepository customerRepository,
                             PaymentRepository paymentRepository,
                             RefundRepository refundRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    public DashboardStats getStats() {
        BigDecimal received = BigDecimal.ZERO;
        BigDecimal due = BigDecimal.ZERO;
        List<PendingPaymentRow> pending = new ArrayList<>();
        for (Payment payment : paymentRepository.findAllByOrderByPaymentDateDesc()) {
            if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                received = received.add(payment.getAmount());
            } else if (payment.getStatus() == PaymentStatus.PENDING
                    && payment.getBooking().getStatus() == BookingStatus.CONFIRMED) {
                // Money still to be collected at pickup for confirmed bookings
                due = due.add(payment.getAmount());
                pending.add(new PendingPaymentRow(
                        payment.getBooking().getCustomer().getUser().getUserName(), payment.getAmount()));
            }
        }
        BigDecimal refunded = BigDecimal.ZERO;
        for (Refund refund : refundRepository.findAll()) {
            refunded = refunded.add(refund.getRefundAmount());
        }
        return new DashboardStats(
                carRepository.count(),
                carRepository.findByStatus(CarStatus.AVAILABLE).size(),
                carRepository.findByStatus(CarStatus.UNDER_MAINTENANCE).size(),
                customerRepository.count(),
                bookingRepository.countByStatus(BookingStatus.PENDING),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.countByStatus(BookingStatus.COMPLETED),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                received, due, refunded, pending);
    }
}