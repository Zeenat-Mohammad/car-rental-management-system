package com.carrental.service;

import com.carrental.dto.CarForm;
import com.carrental.entity.Car;
import com.carrental.exception.BusinessException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.MaintenanceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class CarService {

    private static final Path UPLOAD_DIR = Paths.get("uploads", "cars");
    private static final String URL_PREFIX = "/uploads/cars/";

    public static final List<String> CATEGORIES =
            List.of("Hatchback", "Sedan", "SUV", "MUV", "Luxury", "Convertible");

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceRepository maintenanceRepository;

    public CarService(CarRepository carRepository,
                      BookingRepository bookingRepository,
                      MaintenanceRepository maintenanceRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    public List<Car> findAll() {
        return carRepository.findAll(Sort.by(Sort.Direction.DESC, "carId"));
    }

    public Car getById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Car not found."));
    }

    public boolean registrationExists(String registrationNumber, Long excludeCarId) {
        return carRepository.findByRegistrationNumber(normalizeRegistration(registrationNumber))
                .map(existing -> !existing.getCarId().equals(excludeCarId))
                .orElse(false);
    }

    public CarForm toForm(Car car) {
        CarForm form = new CarForm();
        form.setBrand(car.getBrand());
        form.setModel(car.getModel());
        form.setRegistrationNumber(car.getRegistrationNumber());
        form.setCategory(car.getCategory());
        form.setColor(car.getColor());
        form.setManufactureYear(car.getManufactureYear());
        form.setFuelType(car.getFuelType());
        form.setTransmission(car.getTransmission());
        form.setTotalSeat(car.getTotalSeat());
        form.setRate(car.getRate());
        form.setDescription(car.getDescription());
        form.setStatus(car.getStatus());
        form.setExistingImageUrl(car.getImageUrl());
        return form;
    }

    @Transactional
    public Car createCar(CarForm form) {
        Car car = new Car();
        copyFields(form, car);
        saveImageIfPresent(form, car);
        return carRepository.save(car);
    }

    @Transactional
    public Car updateCar(Long id, CarForm form) {
        Car car = getById(id);
        copyFields(form, car);
        saveImageIfPresent(form, car);
        return carRepository.save(car);
    }

    @Transactional
    public void deleteCar(Long id) {
        Car car = getById(id);
        if (bookingRepository.existsByCar(car)) {
            throw new BusinessException(
                    "This car has bookings and cannot be deleted. Set its status to Under maintenance instead.");
        }
        if (maintenanceRepository.existsByCar(car)) {
            throw new BusinessException(
                    "This car has maintenance records and cannot be deleted. Set its status to Under maintenance instead.");
        }
        carRepository.delete(car);
        deleteImageFile(car.getImageUrl());
    }

    public static boolean isAllowedImageType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }

    private void copyFields(CarForm form, Car car) {
        car.setBrand(form.getBrand());
        car.setModel(form.getModel());
        car.setRegistrationNumber(normalizeRegistration(form.getRegistrationNumber()));
        car.setCategory(form.getCategory());
        car.setColor(form.getColor());
        car.setManufactureYear(form.getManufactureYear());
        car.setFuelType(form.getFuelType());
        car.setTransmission(form.getTransmission());
        car.setTotalSeat(form.getTotalSeat());
        car.setRate(form.getRate().setScale(2, RoundingMode.HALF_UP));
        car.setDescription(form.getDescription());
        car.setStatus(form.getStatus());
    }

    private void saveImageIfPresent(CarForm form, Car car) {
        MultipartFile image = form.getImage();
        if (image == null || image.isEmpty()) {
            return;
        }
        String oldUrl = car.getImageUrl();
        car.setImageUrl(storeImage(image));
        deleteImageFile(oldUrl);
    }

    private String storeImage(MultipartFile file) {
        String fileName = UUID.randomUUID() + extensionFor(file.getContentType());
        try {
            Files.createDirectories(UPLOAD_DIR);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, UPLOAD_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("The image could not be saved. Please try again.");
        }
        return URL_PREFIX + fileName;
    }

    private String extensionFor(String contentType) {
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(URL_PREFIX)) {
            return;
        }
        try {
            Files.deleteIfExists(UPLOAD_DIR.resolve(imageUrl.substring(URL_PREFIX.length())));
        } catch (IOException ignored) {

        }
    }

    private String normalizeRegistration(String registrationNumber) {
        return registrationNumber.replaceAll("[ -]", "").toUpperCase();
    }
}