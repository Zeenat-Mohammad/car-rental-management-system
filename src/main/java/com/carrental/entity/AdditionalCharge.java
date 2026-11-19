package com.carrental.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "additional_charge")
public class AdditionalCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "additional_charge_id")
    private Long additionalChargeId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    public AdditionalCharge() {
    }

    public Long getAdditionalChargeId() { return additionalChargeId; }
    public void setAdditionalChargeId(Long additionalChargeId) { this.additionalChargeId = additionalChargeId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
