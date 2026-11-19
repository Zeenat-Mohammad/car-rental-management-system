package com.carrental.controller;

import com.carrental.dto.CarForm;
import com.carrental.entity.Car;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.exception.BusinessException;
import com.carrental.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;

@Controller
@RequestMapping("/admin/cars")
public class AdminCarController {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final CarService carService;

    public AdminCarController(CarService carService) {
        this.carService = carService;
    }

    @InitBinder("carForm")
    public void initBinder(WebDataBinder binder) {
        for (String field : new String[]{"brand", "model", "registrationNumber", "color", "description"}) {
            binder.registerCustomEditor(String.class, field, new StringTrimmerEditor(true));
        }
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cars", carService.findAll());
        return "admin/cars";
    }

    @GetMapping("/new")
    public String newCarForm(Model model) {
        model.addAttribute("carForm", new CarForm());
        return showForm(model, "Add Car", "/admin/cars");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("carForm") CarForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        checkExtras(form, result, null);
        if (result.hasErrors()) {
            return showForm(model, "Add Car", "/admin/cars");
        }
        try {
            carService.createCar(form);
        } catch (BusinessException e) {
            result.reject("car.failed", e.getMessage());
            return showForm(model, "Add Car", "/admin/cars");
        }
        redirect.addFlashAttribute("success", "Car added successfully.");
        return "redirect:/admin/cars";
    }

    // ---------- edit ----------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Car car = carService.getById(id);
            model.addAttribute("carForm", carService.toForm(car));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/cars";
        }
        return showForm(model, "Edit Car", "/admin/cars/" + id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("carForm") CarForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        Car existing;
        try {
            existing = carService.getById(id);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/cars";
        }
        form.setExistingImageUrl(existing.getImageUrl());

        checkExtras(form, result, id);
        if (result.hasErrors()) {
            return showForm(model, "Edit Car", "/admin/cars/" + id);
        }
        try {
            carService.updateCar(id, form);
        } catch (BusinessException e) {
            result.reject("car.failed", e.getMessage());
            return showForm(model, "Edit Car", "/admin/cars/" + id);
        }
        redirect.addFlashAttribute("success", "Car updated successfully.");
        return "redirect:/admin/cars";
    }

    // ---------- delete ----------

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            carService.deleteCar(id);
            redirect.addFlashAttribute("success", "Car deleted.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cars";
    }

    // ---------- helpers ----------

    // Checks that need more than a simple annotation. Only run when the field has no error yet.
    private void checkExtras(CarForm form, BindingResult result, Long carId) {
        if (!result.hasFieldErrors("manufactureYear") && form.getManufactureYear() != null
                && form.getManufactureYear() > Year.now().getValue() + 1) {
            result.rejectValue("manufactureYear", "future", "Manufacture year cannot be in the future");
        }
        if (!result.hasFieldErrors("registrationNumber")
                && carService.registrationExists(form.getRegistrationNumber(), carId)) {
            result.rejectValue("registrationNumber", "duplicate", "This registration number is already registered");
        }
        MultipartFile image = form.getImage();
        if (image != null && !image.isEmpty()) {
            if (!CarService.isAllowedImageType(image.getContentType())) {
                result.rejectValue("image", "type", "Only JPG, PNG or WEBP images are allowed");
            } else if (image.getSize() > MAX_IMAGE_BYTES) {
                result.rejectValue("image", "size", "Image must be 5 MB or smaller");
            }
        }
    }

    private String showForm(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categories", CarService.CATEGORIES);
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("statuses", CarStatus.values());
        return "admin/car-form";
    }
}