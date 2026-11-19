package com.carrental.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class BookingForm {

    @NotNull(message = "Pickup date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDateTime;

    @NotNull(message = "Return date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime returnDateTime;

    public LocalDateTime getPickupDateTime() { return pickupDateTime; }
    public void setPickupDateTime(LocalDateTime pickupDateTime) { this.pickupDateTime = pickupDateTime; }

    public LocalDateTime getReturnDateTime() { return returnDateTime; }
    public void setReturnDateTime(LocalDateTime returnDateTime) { this.returnDateTime = returnDateTime; }
}