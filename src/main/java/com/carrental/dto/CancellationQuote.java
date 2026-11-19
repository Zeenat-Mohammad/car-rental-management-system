package com.carrental.dto;

import java.math.BigDecimal;

// What happens if a confirmed booking is cancelled right now
public class CancellationQuote {

    private final int percent;
    private final String ruleText;
    private final BigDecimal rentalCost;
    private final BigDecimal calculatedCharge;
    private final BigDecimal amountPaid;
    private final BigDecimal chargeApplied;
    private final BigDecimal refundAmount;

    public CancellationQuote(int percent, String ruleText, BigDecimal rentalCost, BigDecimal calculatedCharge,
                             BigDecimal amountPaid, BigDecimal chargeApplied, BigDecimal refundAmount) {
        this.percent = percent;
        this.ruleText = ruleText;
        this.rentalCost = rentalCost;
        this.calculatedCharge = calculatedCharge;
        this.amountPaid = amountPaid;
        this.chargeApplied = chargeApplied;
        this.refundAmount = refundAmount;
    }

    public int getPercent() { return percent; }
    public String getRuleText() { return ruleText; }
    public BigDecimal getRentalCost() { return rentalCost; }
    public BigDecimal getCalculatedCharge() { return calculatedCharge; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getChargeApplied() { return chargeApplied; }
    public BigDecimal getRefundAmount() { return refundAmount; }
}