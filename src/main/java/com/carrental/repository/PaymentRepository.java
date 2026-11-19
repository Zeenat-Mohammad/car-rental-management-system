package com.carrental.repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingOrderByPaymentDateDesc(Booking booking);

    List<Payment> findByBooking_CustomerOrderByPaymentDateDesc(Customer customer);

    List<Payment> findAllByOrderByPaymentDateDesc();

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);
}