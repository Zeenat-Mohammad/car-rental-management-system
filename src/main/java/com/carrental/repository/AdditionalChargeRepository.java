package com.carrental.repository;

import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdditionalChargeRepository extends JpaRepository<AdditionalCharge, Long> {

    List<AdditionalCharge> findByBooking(Booking booking);
}