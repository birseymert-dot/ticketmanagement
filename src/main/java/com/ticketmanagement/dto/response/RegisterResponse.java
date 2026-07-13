package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;

public class RegisterResponse {

    private String message;
    private String username;
    private Role role;
    private Department department;
    private String profileImage;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String username, Role role) {
        this(message, username, role, null, null);
    }

    public RegisterResponse(String message, String username, Role role, Department department) {
        this(message, username, role, department, null);
    }

    public RegisterResponse(String message, String username, Role role, Department department, String profileImage) {
        this.message = message;
        this.username = username;
        this.role = role;
        this.department = department;
        this.profileImage = profileImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
