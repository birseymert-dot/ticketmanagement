package com.ticketmanagement.dto.request;

import com.ticketmanagement.model.enums.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Kullanici adi bos olamaz")
    @Size(min = 3, max = 50, message = "Kullanici adi 3-50 karakter olmalidir")
    private String username;

    @NotBlank(message = "Email bos olamaz")
    @Email(message = "Gecerli bir email adresi giriniz")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email formati ornek@domain.com seklinde olmalidir"
    )
    private String email;

    @NotBlank(message = "Sifre bos olamaz")
    @Size(min = 6, message = "Sifre en az 6 karakter olmalidir")
    @Pattern(
            regexp = "^(?=(?:.*[A-Za-zÇĞİÖŞÜçğıöşü]){3,}).*$",
            message = "Sifre en az 3 harf icermelidir"
    )
    private String password;

    @NotNull(message = "Departman secimi zorunludur")
    private Department department;

    @Size(max = 300000, message = "Profil fotografi cok buyuk")
    private String profileImage;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage != null ? profileImage.trim() : null;
    }
}
