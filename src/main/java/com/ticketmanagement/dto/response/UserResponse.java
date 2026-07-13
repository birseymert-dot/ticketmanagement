package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Department department;
    private String profileImage;
    private long createdTicketCount;
    private long assignedTicketCount;
    private long commentCount;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.email = user.getEmail();
        response.role = user.getRole();
        response.department = user.getDepartment();
        response.profileImage = user.getProfileImage();
        return response;
    }

    public static UserResponse detailed(User user,
                                        long createdTicketCount,
                                        long assignedTicketCount,
                                        long commentCount) {
        UserResponse response = from(user);
        response.createdTicketCount = createdTicketCount;
        response.assignedTicketCount = assignedTicketCount;
        response.commentCount = commentCount;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Department getDepartment() {
        return department;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public long getCreatedTicketCount() {
        return createdTicketCount;
    }

    public long getAssignedTicketCount() {
        return assignedTicketCount;
    }

    public long getCommentCount() {
        return commentCount;
    }
}
