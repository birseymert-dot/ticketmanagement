package com.ticketmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Size(min = 3, message = "Sifre en az 3 karakter olmalidir")
    @Pattern(
            regexp = ".*[^A-Za-zÇĞİÖŞÜçğıöşü].*",
            message = "Sifre tamamen harflerden olusamaz; en az bir rakam veya sembol icermelidir"
    )
    private String password;

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
}
