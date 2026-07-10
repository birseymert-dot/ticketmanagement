package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.entity.Ticket;
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
    private String assignedTo;
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
        response.assignedTo = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
}
