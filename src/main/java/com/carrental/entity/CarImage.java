package com.carrental.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "car_image")
public class CarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    // When a car is deleted, MySQL deletes its extra photo rows too
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Car car;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    public CarImage() {
    }

    public Long getImageId() { return imageId; }
    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}