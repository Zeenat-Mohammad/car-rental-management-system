package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarImageRepository extends JpaRepository<CarImage, Long> {

    List<CarImage> findByCarOrderByImageIdAsc(Car car);

    Optional<CarImage> findByImageIdAndCar(Long imageId, Car car);

    long countByCar(Car car);
}