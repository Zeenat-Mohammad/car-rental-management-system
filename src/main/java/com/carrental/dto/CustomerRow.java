package com.carrental.dto;

import com.carrental.entity.Customer;

// One line of the admin customer list: the customer and how many bookings they have
public class CustomerRow {

    private final Customer customer;
    private final int bookingCount;

    public CustomerRow(Customer customer, int bookingCount) {
        this.customer = customer;
        this.bookingCount = bookingCount;
    }

    public Customer getCustomer() { return customer; }
    public int getBookingCount() { return bookingCount; }
}