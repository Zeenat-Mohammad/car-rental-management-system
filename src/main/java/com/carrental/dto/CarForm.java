package com.carrental.dto;

import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class CarForm {

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand can have at most 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model can have at most 50 characters")
    private String model;
    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^(?=(?:[ -]*[A-Za-z0-9]){8,11}[ -]*$)[A-Za-z]{2}[A-Za-z0-9 -]*[0-9]{4}$",
            message = "Enter a valid registration number, for example MH12 AB 1234")
    private String registrationNumber;

    @NotBlank(message = "Select a category")
    private String category;

    @NotBlank(message = "Color is required")
    @Size(max = 30, message = "Color can have at most 30 characters")
    private String color;

    @NotNull(message = "Manufacture year is required")
    @Min(value = 1990, message = "Manufacture year must be 1990 or later")
    private Integer manufactureYear;

    @NotNull(message = "Select a fuel type")
    private FuelType fuelType;

    @NotNull(message = "Select a transmission")
    private Transmission transmission;

    @NotNull(message = "Number of seats is required")
    @Min(value = 2, message = "Seats must be between 2 and 15")
    @Max(value = 15, message = "Seats must be between 2 and 15")
    private Integer totalSeat;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "1.00", message = "Daily rate must be between 1 and 99999.99")
    @DecimalMax(value = "99999.99", message = "Daily rate must be between 1 and 99999.99")
    private BigDecimal rate;

    @Size(max = 2000, message = "Description can have at most 2000 characters")
    private String description;

    @NotNull(message = "Select a status")
    private CarStatus status = CarStatus.AVAILABLE;

    private MultipartFile image;

    private String existingImageUrl;

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

    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }

    public String getExistingImageUrl() { return existingImageUrl; }
    public void setExistingImageUrl(String existingImageUrl) { this.existingImageUrl = existingImageUrl; }
}