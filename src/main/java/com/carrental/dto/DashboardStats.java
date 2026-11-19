package com.carrental.dto;

import java.math.BigDecimal;
import java.util.List;

// The numbers shown on the admin dashboard
public class DashboardStats {

    private final long totalCars;
    private final long availableCars;
    private final long carsUnderMaintenance;
    private final long totalCustomers;
    private final long pendingBookings;
    private final long confirmedBookings;
    private final long completedBookings;
    private final long cancelledBookings;
    private final BigDecimal paymentsReceived;
    private final BigDecimal cashDueAtPickup;
    private final BigDecimal totalRefunded;
    private final List<PendingPaymentRow> pendingPayments;

    public DashboardStats(long totalCars, long availableCars, long carsUnderMaintenance, long totalCustomers,
                          long pendingBookings, long confirmedBookings, long completedBookings, long cancelledBookings,
                          BigDecimal paymentsReceived, BigDecimal cashDueAtPickup, BigDecimal totalRefunded,
                          List<PendingPaymentRow> pendingPayments) {
        this.totalCars = totalCars;
        this.availableCars = availableCars;
        this.carsUnderMaintenance = carsUnderMaintenance;
        this.totalCustomers = totalCustomers;
        this.pendingBookings = pendingBookings;
        this.confirmedBookings = confirmedBookings;
        this.completedBookings = completedBookings;
        this.cancelledBookings = cancelledBookings;
        this.paymentsReceived = paymentsReceived;
        this.cashDueAtPickup = cashDueAtPickup;
        this.totalRefunded = totalRefunded;
        this.pendingPayments = pendingPayments;
    }

    public long getTotalCars() { return totalCars; }
    public long getAvailableCars() { return availableCars; }
    public long getCarsUnderMaintenance() { return carsUnderMaintenance; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getPendingBookings() { return pendingBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getCompletedBookings() { return completedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public BigDecimal getPaymentsReceived() { return paymentsReceived; }
    public BigDecimal getCashDueAtPickup() { return cashDueAtPickup; }
    public BigDecimal getTotalRefunded() { return totalRefunded; }
    public List<PendingPaymentRow> getPendingPayments() { return pendingPayments; }
}