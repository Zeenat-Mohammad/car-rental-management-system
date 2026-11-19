package com.carrental.controller;

import com.carrental.dto.MaintenanceForm;
import com.carrental.exception.BusinessException;
import com.carrental.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {

    private final MaintenanceService maintenanceService;

    public AdminMaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @InitBinder("maintenanceForm")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "issues", new StringTrimmerEditor(true));
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("records", maintenanceService.findAll(status));
        model.addAttribute("selectedStatus", status == null ? "" : status.toUpperCase());
        return "admin/maintenance";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long carId, Model model) {
        MaintenanceForm form = new MaintenanceForm();
        form.setCarId(carId);
        model.addAttribute("maintenanceForm", form);
        return showForm(model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("maintenanceForm") MaintenanceForm form,
                         BindingResult result,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model);
        }
        try {
            maintenanceService.create(form, authentication.getName());
        } catch (BusinessException e) {
            result.reject("maintenance.failed", e.getMessage());
            return showForm(model);
        }
        redirect.addFlashAttribute("success", "Maintenance recorded. The car cannot be booked for this period.");
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           Authentication authentication,
                           RedirectAttributes redirect) {
        try {
            maintenanceService.complete(id, authentication.getName());
            redirect.addFlashAttribute("success", "Maintenance completed. The car is available again.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/maintenance";
    }

    private String showForm(Model model) {
        model.addAttribute("cars", maintenanceService.findAllCars());
        return "admin/maintenance-form";
    }
}