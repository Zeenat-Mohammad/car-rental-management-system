package com.carrental.entity;

import com.carrental.enums.BookingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking", indexes = {
        @Index(name = "idx_booking_car_dates", columnList = "car_id, pickup_date_time, return_date_time")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "pickup_date_time", nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(name = "return_date_time", nullable = false)
    private LocalDateTime returnDateTime;

    @Column(name = "actual_return_date_time")
    private LocalDateTime actualReturnDateTime;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    // Rental amount only (total_days x daily rate). Additional charges are stored separately.
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private BookingStatus status = BookingStatus.PENDING;

    public Booking() {
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public LocalDateTime getPickupDateTime() { return pickupDateTime; }
    public void setPickupDateTime(LocalDateTime pickupDateTime) { this.pickupDateTime = pickupDateTime; }

    public LocalDateTime getReturnDateTime() { return returnDateTime; }
    public void setReturnDateTime(LocalDateTime returnDateTime) { this.returnDateTime = returnDateTime; }

    public LocalDateTime getActualReturnDateTime() { return actualReturnDateTime; }
    public void setActualReturnDateTime(LocalDateTime actualReturnDateTime) { this.actualReturnDateTime = actualReturnDateTime; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
