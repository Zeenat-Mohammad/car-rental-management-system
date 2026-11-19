package com.carrental.entity;

import com.carrental.enums.MaintenanceStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Long maintenanceId;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "issues", nullable = false, length = 500)
    private String issues;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private MaintenanceStatus status = MaintenanceStatus.ONGOING;

    public Maintenance() {
    }

    public Long getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(Long maintenanceId) { this.maintenanceId = maintenanceId; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public String getIssues() { return issues; }
    public void setIssues(String issues) { this.issues = issues; }

    public MaintenanceStatus getStatus() { return status; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
}
