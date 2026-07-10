package com.ticketmanagement.dto.request;

import com.ticketmanagement.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StatusUpdateRequest {

    @NotNull(message = "Status bos olamaz (OPEN, IN_PROGRESS, HOLD, DONE)")
    private TicketStatus status;

    /** HOLD'a gecerken zorunlu: ticket'in neden beklemeye alindigi. */
    @Size(max = 500, message = "Bekleme nedeni en fazla 500 karakter olabilir")
    private String reason;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
