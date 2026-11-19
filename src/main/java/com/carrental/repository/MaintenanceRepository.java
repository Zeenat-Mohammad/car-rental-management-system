package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.Maintenance;
import com.carrental.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    List<Maintenance> findAllByOrderByStartDateTimeDesc();

    List<Maintenance> findByCarOrderByStartDateTimeDesc(Car car);

    boolean existsByCar(Car car);

    boolean existsByCarAndStatus(Car car, MaintenanceStatus status);

    @Query("SELECT COUNT(m) FROM Maintenance m "
            + "WHERE m.car.carId = :carId AND m.status = :status "
            + "AND m.startDateTime < :returnTime "
            + "AND (m.endDateTime IS NULL OR m.endDateTime > :pickupTime)")
    long countOverlapping(@Param("carId") Long carId,
                          @Param("status") MaintenanceStatus status,
                          @Param("pickupTime") LocalDateTime pickupTime,
                          @Param("returnTime") LocalDateTime returnTime);

    default boolean hasOngoingOverlap(Long carId, LocalDateTime pickupTime, LocalDateTime returnTime) {
        return countOverlapping(carId, MaintenanceStatus.ONGOING, pickupTime, returnTime) > 0;
    }
}