package com.carrental.repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerOrderByPickupDateTimeDesc(Customer customer);

    List<Booking> findAllByOrderByPickupDateTimeDesc();

    long countByStatus(BookingStatus status);

    boolean existsByCar(Car car);

    @Query("SELECT COUNT(b) FROM Booking b "
            + "WHERE b.car.carId = :carId AND b.status = :status "
            + "AND b.pickupDateTime < :returnTime AND b.returnDateTime > :pickupTime")
    long countOverlapping(@Param("carId") Long carId,
                          @Param("status") BookingStatus status,
                          @Param("pickupTime") LocalDateTime pickupTime,
                          @Param("returnTime") LocalDateTime returnTime);
    default boolean hasConfirmedOverlap(Long carId, LocalDateTime pickupTime, LocalDateTime returnTime) {
        return countOverlapping(carId, BookingStatus.CONFIRMED, pickupTime, returnTime) > 0;
    }
}