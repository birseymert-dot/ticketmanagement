package com.ticketmanagement.model.entity;

import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    /**
     * Kullanici ID'leri veritabani sayaciyla degil, uygulama tarafindan
     * atanir: yeni kayit her zaman en kucuk bos ID'yi alir. Boylece
     * silinen kullanicilarin numaralari yeniden kullanilir ve numaralar
     * sistematik ilerler (bkz. AuthServiceImpl.nextFreeUserId).
     */
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Kullanicinin bagli oldugu ust birim; ADMIN icin null. */
    @Enumerated(EnumType.STRING)
    private Department department;

    /** Opsiyonel profil fotografi. Frontend kucuk boyutlu data URL olarak gonderir. */
    @Lob
    @Column(name = "profile_image")
    private String profileImage;

    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this(username, email, password, role, null);
    }

    public User(String username, String email, String password, Role role, Department department) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
        this.profileImage = profileImage;
    }
}
