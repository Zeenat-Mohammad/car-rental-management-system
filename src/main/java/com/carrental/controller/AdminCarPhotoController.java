package com.carrental.controller;

import com.carrental.entity.Car;
import com.carrental.exception.BusinessException;
import com.carrental.service.CarGalleryService;
import com.carrental.service.CarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cars/{id}/photos")
public class AdminCarPhotoController {

    private final CarService carService;
    private final CarGalleryService carGalleryService;

    public AdminCarPhotoController(CarService carService, CarGalleryService carGalleryService) {
        this.carService = carService;
        this.carGalleryService = carGalleryService;
    }

    @GetMapping
    public String page(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Car car = carService.getById(id);
            model.addAttribute("car", car);
            model.addAttribute("images", carGalleryService.extraImages(car));
            model.addAttribute("maxPhotos", CarGalleryService.MAX_EXTRA_PHOTOS);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/cars";
        }
        return "admin/car-photos";
    }

    @PostMapping
    public String upload(@PathVariable Long id,
                         @RequestParam("files") MultipartFile[] files,
                         RedirectAttributes redirect) {
        try {
            int n = carGalleryService.addImages(carService.getById(id), files);
            redirect.addFlashAttribute("success", n + (n == 1 ? " photo added." : " photos added."));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cars/" + id + "/photos";
    }

    @PostMapping("/{imageId}/delete")
    public String delete(@PathVariable Long id, @PathVariable Long imageId, RedirectAttributes redirect) {
        try {
            carGalleryService.deleteImage(carService.getById(id), imageId);
            redirect.addFlashAttribute("success", "Photo deleted.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cars/" + id + "/photos";
    }
}