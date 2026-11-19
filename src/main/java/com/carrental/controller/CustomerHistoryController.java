package com.carrental.controller;

import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.enums.BookingStatus;
import com.carrental.service.BookingService;
import com.carrental.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Booking history: the customer's finished bookings (completed or cancelled)
@Controller
@RequestMapping("/customer/bookings/history")
public class CustomerHistoryController {

    private final BookingService bookingService;
    private final CustomerService customerService;

    public CustomerHistoryController(BookingService bookingService, CustomerService customerService) {
        this.bookingService = bookingService;
        this.customerService = customerService;
    }

    @GetMapping
    public String history(Authentication authentication, Model model) {
        Customer customer = customerService.getByEmail(authentication.getName());
        List<Booking> finished = bookingService.findByCustomer(customer).stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CANCELLED)
                .toList();

        // Additional charges of each booking (late return, cancellation charge...)
        Map<Long, BigDecimal> extras = new HashMap<>();
        for (Booking booking : finished) {
            extras.put(booking.getBookingId(), bookingService.sumCharges(bookingService.getCharges(booking)));
        }
        model.addAttribute("bookings", finished);
        model.addAttribute("extras", extras);
        return "customer/booking-history";
    }
}