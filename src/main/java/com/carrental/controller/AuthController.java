package com.carrental.controller;

import com.carrental.dto.RegistrationForm;
import com.carrental.service.UserService;
import com.carrental.validation.RegistrationValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final RegistrationValidator registrationValidator;

    public AuthController(UserService userService, RegistrationValidator registrationValidator) {
        this.userService = userService;
        this.registrationValidator = registrationValidator;
    }

    // Trims spaces around the text fields (not the passwords) before validation
    @InitBinder("registrationForm")
    public void initBinder(WebDataBinder binder) {
        for (String field : new String[]{"userName", "email", "phoneNumber", "address", "licenseNumber"}) {
            binder.registerCustomEditor(String.class, field, new StringTrimmerEditor(true));
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result) {

        // 1. Required, then length, then format. One message per field.
        registrationValidator.validate(form, result);

        // 2. Passwords must match (only checked if "confirm password" has no error yet)
        if (!result.hasFieldErrors("confirmPassword")
                && form.getPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        // 3. Duplicates (only checked if the field has no error yet)
        if (!result.hasFieldErrors("email") && userService.emailExists(form.getEmail())) {
            result.rejectValue("email", "duplicate", "This email is already registered");
        }
        if (!result.hasFieldErrors("licenseNumber") && userService.licenseExists(form.getLicenseNumber())) {
            result.rejectValue("licenseNumber", "duplicate", "This license number is already registered");
        }
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerCustomer(form);
        } catch (DataIntegrityViolationException e) {
            // Safety net if two people register the same email at the same moment
            result.reject("registration.failed", "Registration failed. The email or license number may already be in use.");
            return "register";
        }
        return "redirect:/login?registered";
    }
}