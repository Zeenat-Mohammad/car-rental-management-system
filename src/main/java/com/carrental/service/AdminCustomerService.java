package com.carrental.service;

import com.carrental.dto.CustomerRow;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.exception.BusinessException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    public AdminCustomerService(CustomerRepository customerRepository, BookingRepository bookingRepository) {
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
    }

    // All customers, A to Z. A keyword searches name, email, phone and licence number.
    public List<CustomerRow> search(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        return customerRepository.findAll().stream()
                .filter(c -> key.isEmpty() || matches(c, key))
                .sorted(Comparator.comparing(c -> c.getUser().getUserName().toLowerCase()))
                .map(c -> new CustomerRow(c, bookingRepository.findByCustomerOrderByPickupDateTimeDesc(c).size()))
                .toList();
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Customer not found."));
    }

    public List<Booking> getBookings(Customer customer) {
        return bookingRepository.findByCustomerOrderByPickupDateTimeDesc(customer);
    }

    private boolean matches(Customer c, String key) {
        return c.getUser().getUserName().toLowerCase().contains(key)
                || c.getUser().getEmail().toLowerCase().contains(key)
                || c.getPhoneNumber().toLowerCase().contains(key)
                || c.getLicenseNumber().toLowerCase().contains(key);
    }
}