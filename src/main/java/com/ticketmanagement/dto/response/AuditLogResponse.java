package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.entity.AuditLog;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private AuditAction action;
    private String username;
    private Role userRole;
    private Department userDepartment;
    private String userProfileImage;
    private Long ticketId;
    private String details;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog log, User user) {
        AuditLogResponse response = new AuditLogResponse();
        response.id = log.getId();
        response.action = log.getAction();
        response.username = log.getUsername();
        response.ticketId = log.getTicketId();
        response.details = log.getDetails();
        response.timestamp = log.getTimestamp();

        if (user != null) {
            response.userRole = user.getRole();
            response.userDepartment = user.getDepartment();
            response.userProfileImage = user.getProfileImage();
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public Role getUserRole() {
        return userRole;
    }

    public Department getUserDepartment() {
        return userDepartment;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
