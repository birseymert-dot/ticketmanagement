package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.enums.Role;

public class RegisterResponse {

    private String message;
    private String username;
    private Role role;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String username, Role role) {
        this.message = message;
        this.username = username;
        this.role = role;
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
}

