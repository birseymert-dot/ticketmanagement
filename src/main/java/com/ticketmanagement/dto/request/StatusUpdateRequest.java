package com.ticketmanagement.dto.request;

import com.ticketmanagement.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status bos olamaz (OPEN, IN_PROGRESS, DONE)")
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
