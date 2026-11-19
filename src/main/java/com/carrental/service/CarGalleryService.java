package com.carrental.service;

import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import com.carrental.exception.BusinessException;
import com.carrental.repository.CarImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Extra photos for a car. The cover photo stays in Car.imageUrl.
@Service
public class CarGalleryService {

    private static final Path UPLOAD_DIR = Paths.get("uploads", "cars");
    private static final String URL_PREFIX = "/uploads/cars/";
    public static final int MAX_EXTRA_PHOTOS = 8;
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final CarImageRepository carImageRepository;

    public CarGalleryService(CarImageRepository carImageRepository) {
        this.carImageRepository = carImageRepository;
    }

    public List<CarImage> extraImages(Car car) {
        return carImageRepository.findByCarOrderByImageIdAsc(car);
    }

    // Cover photo first, then the extra photos. Used by the car details page.
    public List<String> photoUrls(Car car) {
        List<String> urls = new ArrayList<>();
        if (car.getImageUrl() != null) {
            urls.add(car.getImageUrl());
        }
        for (CarImage image : extraImages(car)) {
            urls.add(image.getImageUrl());
        }
        return urls;
    }

    @Transactional
    public int addImages(Car car, MultipartFile[] files) {
        List<MultipartFile> valid = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    valid.add(f);
                }
            }
        }
        if (valid.isEmpty()) {
            throw new BusinessException("Please choose at least one photo.");
        }
        if (carImageRepository.countByCar(car) + valid.size() > MAX_EXTRA_PHOTOS) {
            throw new BusinessException("A car can have at most " + MAX_EXTRA_PHOTOS + " extra photos.");
        }
        for (MultipartFile f : valid) {
            if (!CarService.isAllowedImageType(f.getContentType())) {
                throw new BusinessException("Only JPG, PNG or WEBP images are allowed.");
            }
            if (f.getSize() > MAX_BYTES) {
                throw new BusinessException("Each photo must be 5 MB or smaller.");
            }
        }
        for (MultipartFile f : valid) {
            CarImage image = new CarImage();
            image.setCar(car);
            image.setImageUrl(store(f));
            carImageRepository.save(image);
        }
        return valid.size();
    }

    @Transactional
    public void deleteImage(Car car, Long imageId) {
        CarImage image = carImageRepository.findByImageIdAndCar(imageId, car)
                .orElseThrow(() -> new BusinessException("Photo not found."));
        carImageRepository.delete(image);
        try {
            if (image.getImageUrl().startsWith(URL_PREFIX)) {
                Files.deleteIfExists(UPLOAD_DIR.resolve(image.getImageUrl().substring(URL_PREFIX.length())));
            }
        } catch (IOException ignored) {
            // a leftover file is harmless
        }
    }

    private String store(MultipartFile file) {
        String type = file.getContentType();
        String ext = "image/png".equals(type) ? ".png" : "image/webp".equals(type) ? ".webp" : ".jpg";
        String name = UUID.randomUUID() + ext;
        try {
            Files.createDirectories(UPLOAD_DIR);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, UPLOAD_DIR.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("A photo could not be saved. Please try again.");
        }
        return URL_PREFIX + name;
    }
}