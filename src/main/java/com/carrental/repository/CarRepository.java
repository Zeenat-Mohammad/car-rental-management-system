package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);

    Optional<Car> findByRegistrationNumber(String registrationNumber);

    List<Car> findByStatus(CarStatus status);

    @Query("SELECT c FROM Car c WHERE c.status = :status "
            + "AND (LOWER(c.brand) LIKE CONCAT('%', :keyword, '%') "
            + "OR LOWER(c.model) LIKE CONCAT('%', :keyword, '%') "
            + "OR LOWER(CONCAT(c.brand, ' ', c.model)) LIKE CONCAT('%', :keyword, '%')) "
            + "AND (:category = '' OR c.category = :category) "
            + "AND c.rate >= :minPrice AND c.rate <= :maxPrice "
            + "ORDER BY c.rate ASC, c.carId DESC")
    List<Car> search(@Param("status") CarStatus status,
                     @Param("keyword") String keyword,
                     @Param("category") String category,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice);
}