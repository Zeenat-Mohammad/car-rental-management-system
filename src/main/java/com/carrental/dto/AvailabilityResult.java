package com.carrental.dto;

import java.math.BigDecimal;

public class AvailabilityResult {

    private final boolean available;
    private final String message;
    private final Integer totalDays;
    private final BigDecimal rentalCost;

    private AvailabilityResult(boolean available, String message, Integer totalDays, BigDecimal rentalCost) {
        this.available = available;
        this.message = message;
        this.totalDays = totalDays;
        this.rentalCost = rentalCost;
    }

    public static AvailabilityResult ok(int totalDays, BigDecimal rentalCost) {
        return new AvailabilityResult(true, null, totalDays, rentalCost);
    }

    public static AvailabilityResult notAvailable(String message) {
        return new AvailabilityResult(false, message, null, null);
    }

    public boolean isAvailable() { return available; }
    public String getMessage() { return message; }
    public Integer getTotalDays() { return totalDays; }
    public BigDecimal getRentalCost() { return rentalCost; }
}