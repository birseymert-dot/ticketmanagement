package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;

public class AuthResponse {

    private String token;
    private String username;
    private Role role;
    private Department department;
    private String profileImage;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, Role role) {
        this(token, username, role, null, null);
    }

    public AuthResponse(String token, String username, Role role, Department department) {
        this(token, username, role, department, null);
    }

    public AuthResponse(String token, String username, Role role, Department department, String profileImage) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.department = department;
        this.profileImage = profileImage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
