package com.carrental.service;

import com.carrental.dto.MaintenanceForm;
import com.carrental.entity.Admin;
import com.carrental.entity.Car;
import com.carrental.entity.Maintenance;
import com.carrental.entity.User;
import com.carrental.enums.CarStatus;
import com.carrental.enums.MaintenanceStatus;
import com.carrental.exception.BusinessException;
import com.carrental.repository.AdminRepository;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.MaintenanceRepository;
import com.carrental.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MaintenanceService {

    // Used to test a maintenance with no end time against confirmed bookings
    private static final LocalDateTime FAR_FUTURE = LocalDateTime.of(2999, 12, 31, 23, 59);

    private final MaintenanceRepository maintenanceRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository,
                              CarRepository carRepository,
                              BookingRepository bookingRepository,
                              AdminRepository adminRepository,
                              UserRepository userRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    // All records (newest first). status = null, empty or unknown means "all".
    public List<Maintenance> findAll(String status) {
        List<Maintenance> all = maintenanceRepository.findAllByOrderByStartDateTimeDesc();
        if (status == null || status.isBlank()) {
            return all;
        }
        MaintenanceStatus wanted;
        try {
            wanted = MaintenanceStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return all;
        }
        return all.stream().filter(m -> m.getStatus() == wanted).toList();
    }

    public List<Car> findAllCars() {
        return carRepository.findAll(Sort.by(Sort.Direction.ASC, "brand", "model"));
    }

    // Records a new maintenance. The admin is the logged-in admin.
    @Transactional
    public Maintenance create(MaintenanceForm form, String adminEmail) {
        Admin admin = getAdmin(adminEmail);
        Car car = carRepository.findById(form.getCarId())
                .orElseThrow(() -> new BusinessException("Car not found."));

        LocalDateTime start = form.getStartDateTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime end = form.getEndDateTime() == null ? null : form.getEndDateTime().truncatedTo(ChronoUnit.MINUTES);

        if (end != null && !end.isAfter(start)) {
            throw new BusinessException("End date and time must be after the start date and time.");
        }
        if (maintenanceRepository.existsByCarAndStatus(car, MaintenanceStatus.ONGOING)) {
            throw new BusinessException(
                    "This car already has an ongoing maintenance record. Complete it before adding another.");
        }
        // A confirmed booking and a maintenance of the same car must not overlap
        LocalDateTime checkUntil = (end == null) ? FAR_FUTURE : end;
        if (bookingRepository.hasConfirmedOverlap(car.getCarId(), start, checkUntil)) {
            throw new BusinessException(
                    "This car has a confirmed booking during this period. Cancel or complete that booking first.");
        }

        Maintenance maintenance = new Maintenance();
        maintenance.setCar(car);
        maintenance.setAdmin(admin);
        maintenance.setStartDateTime(start);
        maintenance.setEndDateTime(end);
        maintenance.setIssues(form.getIssues().trim());
        maintenance.setStatus(MaintenanceStatus.ONGOING);
        maintenanceRepository.save(maintenance);

        // A maintenance that has already started hides the car from the public list at once.
        // One that starts later is handled by the date overlap check at booking time.
        if (!start.isAfter(LocalDateTime.now())) {
            car.setStatus(CarStatus.UNDER_MAINTENANCE);
            carRepository.save(car);
        }
        return maintenance;
    }

    // The repair is finished. The car is available again unless it has another ongoing record.
    @Transactional
    public void complete(Long maintenanceId, String adminEmail) {
        Admin admin = getAdmin(adminEmail);
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new BusinessException("Maintenance record not found."));
        if (maintenance.getStatus() != MaintenanceStatus.ONGOING) {
            throw new BusinessException("This maintenance is already completed.");
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime start = maintenance.getStartDateTime();
        LocalDateTime end = maintenance.getEndDateTime();
        // Closed now, unless it was planned to end earlier. A maintenance that never started ends at its start.
        if (end == null || end.isAfter(now)) {
            maintenance.setEndDateTime(now.isBefore(start) ? start : now);
        }
        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setAdmin(admin);
        maintenanceRepository.save(maintenance);

        Car car = maintenance.getCar();
        if (car.getStatus() == CarStatus.UNDER_MAINTENANCE
                && !maintenanceRepository.existsByCarAndStatus(car, MaintenanceStatus.ONGOING)) {
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }
    }

    private Admin getAdmin(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Account not found."));
        return adminRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Admin profile not found."));
    }
}