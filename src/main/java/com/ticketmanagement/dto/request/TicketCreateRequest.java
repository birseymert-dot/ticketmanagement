package com.ticketmanagement.dto.request;

import com.ticketmanagement.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketCreateRequest {

    @NotBlank(message = "Title bos olamaz")
    @Size(max = 200, message = "Title en fazla 200 karakter olabilir")
    private String title;

    @NotBlank(message = "Description bos olamaz")
    @Size(max = 2000, message = "Description en fazla 2000 karakter olabilir")
    private String description;

    @NotNull(message = "Priority bos olamaz (LOW, MEDIUM, HIGH)")
    private TicketPriority priority;

    /** Opsiyonel: ticket olusturulurken bir kullaniciya ID ile atanabilir. */
    private Long assignedToId;

    /** Opsiyonel: ID yerine kullanici adi ile atama (arayuz bu alani kullanir). */
    private String assignedToUsername;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
}
