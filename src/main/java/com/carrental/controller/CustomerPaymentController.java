package com.carrental.controller;

import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import com.carrental.enums.PaymentMethod;
import com.carrental.exception.BusinessException;
import com.carrental.service.CustomerService;
import com.carrental.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class CustomerPaymentController {

    private final PaymentService paymentService;
    private final CustomerService customerService;

    public CustomerPaymentController(PaymentService paymentService, CustomerService customerService) {
        this.paymentService = paymentService;
        this.customerService = customerService;
    }

    // option: FULL, ADVANCE (online) or PICKUP (pay at pickup)
    @PostMapping("/bookings/{id}/pay")
    public String pay(@PathVariable Long id,
                      @RequestParam(required = false) String option,
                      @RequestParam(required = false) PaymentMethod method,
                      Authentication authentication,
                      RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            if ("PICKUP".equals(option)) {
                paymentService.payAtPickup(id, customer);
                redirect.addFlashAttribute("success",
                        "Booking confirmed. You will pay when you collect the car.");
                return "redirect:/customer/bookings/" + id;
            }
            Payment payment = paymentService.startOnlinePayment(id, customer, option, method);
            return "redirect:/customer/payments/" + payment.getPaymentId() + "/simulate";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings/" + id;
        }
    }

    // The simulated payment page (stands in for a real payment gateway)
    @GetMapping("/payments/{paymentId}/simulate")
    public String simulatePage(@PathVariable Long paymentId,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        try {
            Payment payment = paymentService.getOpenOnlinePayment(paymentId, customer);
            model.addAttribute("payment", payment);
            model.addAttribute("booking", payment.getBooking());
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings";
        }
        return "customer/payment-simulation";
    }

    @PostMapping("/payments/{paymentId}/simulate")
    public String simulateResult(@PathVariable Long paymentId,
                                 @RequestParam String outcome,
                                 Authentication authentication,
                                 RedirectAttributes redirect) {
        Customer customer = customerService.getByEmail(authentication.getName());
        Long bookingId;
        try {
            bookingId = paymentService.getOpenOnlinePayment(paymentId, customer).getBooking().getBookingId();
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/bookings";
        }
        try {
            paymentService.completeSimulatedPayment(paymentId, customer, "success".equals(outcome));
            redirect.addFlashAttribute("success", "Payment successful. Your booking is confirmed.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/bookings/" + bookingId;
    }
}