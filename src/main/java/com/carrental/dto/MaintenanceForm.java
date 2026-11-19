package com.carrental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class MaintenanceForm {

    @NotNull(message = "Select a car")
    private Long carId;

    @NotBlank(message = "Describe the issue")
    @Size(max = 500, message = "Issue can have at most 500 characters")
    private String issues;

    @NotNull(message = "Start date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDateTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateTime;

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
    public String getIssues() { return issues; }
    public void setIssues(String issues) { this.issues = issues; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
}