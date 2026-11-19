package com.carrental.dto;

import java.math.BigDecimal;

public class PendingPaymentRow {

    private final String customerName;
    private final BigDecimal amount;

    public PendingPaymentRow(String customerName, BigDecimal amount) {
        this.customerName = customerName;
        this.amount = amount;
    }

    public String getCustomerName() { return customerName; }
    public BigDecimal getAmount() { return amount; }
}