package com.carrental.dto;

import com.carrental.validation.ValidationGroups.Format;
import com.carrental.validation.ValidationGroups.Length;
import com.carrental.validation.ValidationGroups.Required;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Each rule belongs to a group. RegistrationValidator checks Required, then Length, then Format,
// and shows only the first error of each field.
public class RegistrationForm {

    // At least 2 letters, starts with a letter. Allowed: letters and spaces only
    @NotBlank(groups = Required.class, message = "Name is required")
    @Size(max = 100, groups = Length.class, message = "Name can have at most 100 characters")
    @Pattern(regexp = "^(?=(?:.*\\p{L}){2})\\p{L}[\\p{L} ]*$", groups = Format.class,
            message = "Name must contain at least 2 letters and only letters and spaces")
    private String userName;

    // Needs a domain with a dot and an ending of 2+ letters, so "a@ams" is rejected
    @NotBlank(groups = Required.class, message = "Email is required")
    @Size(max = 150, groups = Length.class, message = "Email can have at most 150 characters")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", groups = Format.class,
            message = "Enter a valid email address, for example name@example.com")
    private String email;

    @NotBlank(groups = Required.class, message = "Password is required")
    @Size(min = 8, max = 72, groups = Length.class, message = "Password must be 8 to 72 characters")
    private String password;

    @NotBlank(groups = Required.class, message = "Please confirm your password")
    private String confirmPassword;

    // The length (7 to 20) is part of this one format rule
    @NotBlank(groups = Required.class, message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", groups = Format.class, message = "Enter a valid phone number")
    private String phoneNumber;

    @NotBlank(groups = Required.class, message = "Address is required")
    @Size(min = 10, max = 255, groups = Length.class, message = "Address must be 10 to 255 characters")
    @Pattern(regexp = "^(?=.*\\p{L}).+$", groups = Format.class, message = "Address must contain letters, not only numbers")
    private String address;

    // Indian licences vary. After removing spaces, hyphens and slashes the number must start
    // with 2 letters (state code), be 10 to 16 characters long and contain at least 6 digits.
    // The length (10 to 16) is part of this one format rule.
    @NotBlank(groups = Required.class, message = "Driving license number is required")
    @Pattern(regexp = "^(?=(?:[^0-9]*[0-9]){6})(?=(?:[ /-]*[A-Za-z0-9]){10,16}[ /-]*$)[A-Za-z]{2}[A-Za-z0-9 /-]*$",
            groups = Format.class,
            message = "Enter a valid license number, for example MH12 20110012345 (state code, then 10 to 14 letters or digits)")
    private String licenseNumber;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}