package com.carrental.entity;

import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "registration_number", nullable = false, unique = true, length = 30)
    private String registrationNumber;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, columnDefinition = "varchar(20)")
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, columnDefinition = "varchar(20)")
    private Transmission transmission;

    @Column(name = "total_seat", nullable = false)
    private Integer totalSeat;

    @Column(name = "rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private CarStatus status = CarStatus.AVAILABLE;

    public Car() {
    }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public Transmission getTransmission() { return transmission; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }

    public Integer getTotalSeat() { return totalSeat; }
    public void setTotalSeat(Integer totalSeat) { this.totalSeat = totalSeat; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }
}
