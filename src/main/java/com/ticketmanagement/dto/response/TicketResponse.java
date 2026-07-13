package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;

import java.time.LocalDateTime;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String holdReason;
    private String createdBy;
    private Role createdByRole;
    private Department createdByDepartment;
    private String createdByProfileImage;
    private String assignedTo;
    private Role assignedToRole;
    private Department assignedToDepartment;
    private String assignedToProfileImage;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.id = ticket.getId();
        response.title = ticket.getTitle();
        response.description = ticket.getDescription();
        response.status = ticket.getStatus();
        response.priority = ticket.getPriority();
        response.holdReason = ticket.getHoldReason();
        response.createdBy = ticket.getCreatedBy().getUsername();
        response.createdByRole = ticket.getCreatedBy().getRole();
        response.createdByDepartment = ticket.getCreatedBy().getDepartment();
        response.createdByProfileImage = ticket.getCreatedBy().getProfileImage();
        response.assignedTo = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null;
        response.assignedToRole = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getRole() : null;
        response.assignedToDepartment = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getDepartment() : null;
        response.assignedToProfileImage = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getProfileImage() : null;
        response.createdDate = ticket.getCreatedDate();
        response.updatedDate = ticket.getUpdatedDate();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public String getHoldReason() {
        return holdReason;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Role getCreatedByRole() {
        return createdByRole;
    }

    public Department getCreatedByDepartment() {
        return createdByDepartment;
    }

    public String getCreatedByProfileImage() {
        return createdByProfileImage;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Role getAssignedToRole() {
        return assignedToRole;
    }

    public Department getAssignedToDepartment() {
        return assignedToDepartment;
    }

    public String getAssignedToProfileImage() {
        return assignedToProfileImage;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
}
