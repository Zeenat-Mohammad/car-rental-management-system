package com.carrental.entity;

import com.carrental.enums.RefundStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private RefundStatus status;

    @Column(name = "date", nullable = false)
    private LocalDateTime refundDate;

    protected Refund() {
    }

    public Refund(Payment payment, BigDecimal refundAmount, RefundStatus status) {
        this.payment = payment;
        this.booking = payment.getBooking();
        this.refundAmount = refundAmount;
        this.status = status;
        this.refundDate = LocalDateTime.now();
    }

    public Long getRefundId() { return refundId; }

    public Payment getPayment() { return payment; }

    public Booking getBooking() { return booking; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }
}
