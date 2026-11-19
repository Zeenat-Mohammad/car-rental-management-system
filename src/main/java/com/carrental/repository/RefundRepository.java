package com.carrental.repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByBooking(Booking booking);

    List<Refund> findAllByOrderByRefundDateDesc();
}