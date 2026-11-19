package com.carrental.controller;

import com.carrental.dto.CancellationQuote;
import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import com.carrental.enums.BookingStatus;
import com.carrental.exception.BusinessException;
import com.carrental.service.AdminBookingService;
import com.carrental.service.BookingService;
import com.carrental.service.CancellationService;
import com.carrental.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final CancellationService cancellationService;

    public AdminBookingController(AdminBookingService adminBookingService,
                                  BookingService bookingService,
                                  PaymentService paymentService,
                                  CancellationService cancellationService) {
        this.adminBookingService = adminBookingService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.cancellationService = cancellationService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("bookings", adminBookingService.findAll(status));
        model.addAttribute("selectedStatus", status == null ? "" : status.toUpperCase());
        return "admin/bookings";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Booking booking;
        try {
            booking = adminBookingService.getBooking(id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/bookings";
        }

        List<AdditionalCharge> charges = bookingService.getCharges(booking);
        BigDecimal chargesTotal = bookingService.sumCharges(charges);
        BigDecimal grandTotal = booking.getTotalCost().add(chargesTotal);
        BigDecimal paid = paymentService.amountPaid(booking);
        BigDecimal outstanding = grandTotal.subtract(paid).max(BigDecimal.ZERO);

        model.addAttribute("booking", booking);
        model.addAttribute("ratePerDay", bookingService.dailyRate(booking));
        model.addAttribute("charges", charges);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("paidTotal", paid);
        model.addAttribute("outstanding", outstanding);
        model.addAttribute("payments", paymentService.getPayments(booking));
        model.addAttribute("refunds", paymentService.getRefunds(booking));
        model.addAttribute("nowLocal", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString());

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            CancellationQuote quote = cancellationService.quote(booking);
            model.addAttribute("quote", quote);
        }
        return "admin/booking-details";
    }

    @PostMapping("/payments/{paymentId}/received")
    public String cashReceived(@PathVariable Long paymentId, RedirectAttributes redirect) {
        try {
            Long bookingId = adminBookingService.recordCash(paymentId);
            redirect.addFlashAttribute("success", "Cash payment recorded as successful.");
            return "redirect:/admin/bookings/" + bookingId;
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/bookings";
        }
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           @RequestParam(required = false)
                           @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actualReturn,
                           RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("success", adminBookingService.completeBooking(id, actualReturn));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("success", cancellationService.cancelByAdmin(id));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }
}