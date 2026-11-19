package com.carrental.controller;

import com.carrental.dto.AvailabilityResult;
import com.carrental.dto.BookingForm;
import com.carrental.entity.AdditionalCharge;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.enums.BookingStatus;
import com.carrental.exception.BusinessException;
import com.carrental.service.BookingService;
import com.carrental.service.CancellationService;
import com.carrental.service.CarSearchService;
import com.carrental.service.CustomerService;
import com.carrental.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/customer")
public class CustomerBookingController {

    private final BookingService bookingService;
    private final CustomerService customerService;
    private final CarSearchService carSearchService;
    private final PaymentService paymentService;
    private final CancellationService cancellationService;

    public CustomerBookingController(BookingService bookingService,
                                     CustomerService customerService,
                                     CarSearchService carSearchService,
                                     PaymentService paymentService,
                                     CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.carSearchService = carSearchService;
        this.paymentService = paymentService;
        this.cancellationService = cancellationService;
    }

    // ---------- choose dates, check availability, book ----------

    @GetMapping("/cars/{carId}/book")
    public String bookingForm(@PathVariable Long carId, Model model, RedirectAttributes redirect) {
        Car car;
        try {
            car = carSearchService.getById(carId);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars";
        }
        model.addAttribute("bookingForm", new BookingForm());
        return showBookingForm(model, car);
    }

    // The same form has two buttons: action=check (only shows the result) and action=book
    @PostMapping("/cars/{carId}/book")
    public String bookCar(@PathVariable Long carId,
                          @Valid @ModelAttribute("bookingForm") BookingForm form,
                          BindingResult result,
                          @RequestParam(defaultValue = "check") String action,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirect) {
        Car car;
        try {
            car = carSearchService.getById(carId);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars";
        }
        if (result.hasErrors()) {
            return showBookingForm(model, car);
        }

        AvailabilityResult availability =
                bookingService.check(car, form.getPickupDateTime(), form.getReturnDateTime());

        if (!"book".equals(action) || !availability.isAvailable()) {
            model.addAttribute("availability", availability);
            return showBookingForm(model, car);
        }

        try {
            Customer customer = customerService.getByEmail(authentication.getName());
            Booking booking = bookingService.createPendingBooking(
                    customer, car, form.getPickupDateTime(), form.getReturnDateTime());
            redirect.addFlashAttribute("success", "Booking created. Choose how you want to pay to confirm it.");
            return "redirect:/customer/bookings/" + booking.getBookingId();
        } catch (BusinessException e) {
            model.addAttribute("availability", AvailabilityResult.notAvailable(e.getMessage()));
            return showBookingForm(model, car);
        }
    }

    // ---------- my bookings ----------

    @GetMapping("/bookings")
    public String myBookings(Authentication authentication, Model model) {
        Customer customer = customerService.getByEmail(authentication.getName());
        model.addAttribute("bookings", bookingService.findByCustomer(customer));
        return "customer/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            Booking booking = bookingService.getOwnBooking(id, customer);
            List<AdditionalCharge> charges = bookingService.getCharges(booking);
            BigDecimal chargesTotal = bookingService.sumCharges(charges);
            BigDecimal grandTotal = booking.getTotalCost().add(chargesTotal);
            BigDecimal paidTotal = paymentService.amountPaid(booking);

            model.addAttribute("booking", booking);
            model.addAttribute("ratePerDay", bookingService.dailyRate(booking));
            model.addAttribute("charges", charges);
            model.addAttribute("chargesTotal", chargesTotal);
            model.addAttribute("grandTotal", grandTotal);
            model.addAttribute("payments", paymentService.getPayments(booking));
            model.addAttribute("refunds", paymentService.getRefunds(booking));
            model.addAttribute("paidTotal", paidTotal);
            model.addAttribute("dueAmount", grandTotal.subtract(paidTotal).max(BigDecimal.ZERO));
            model.addAttribute("advanceAmount", paymentService.advanceAmount(booking));
            model.addAttribute("balanceAmount", paymentService.balanceAmount(booking));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings";
        }
        return "customer/booking-details";
    }

    // ---------- cancel ----------

    // Shows the charge and the refund before the customer confirms (confirmed bookings only)
    @GetMapping("/bookings/{id}/cancel")
    public String cancelPreview(@PathVariable Long id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            Booking booking = bookingService.getOwnBooking(id, customer);
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                redirect.addFlashAttribute("error", "This booking cannot be cancelled from here.");
                return "redirect:/customer/bookings/" + id;
            }
            model.addAttribute("booking", booking);
            model.addAttribute("quote", cancellationService.quote(booking));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings";
        }
        return "customer/cancel-booking";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            String message = cancellationService.cancel(id, customer);
            redirect.addFlashAttribute("success", message);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }

    // ---------- helper ----------

    private String showBookingForm(Model model, Car car) {
        model.addAttribute("car", car);
        // The date pickers do not allow past times
        model.addAttribute("minDateTime", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString());
        return "customer/book-car";
    }
}