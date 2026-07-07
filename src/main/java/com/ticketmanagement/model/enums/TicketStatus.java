package com.ticketmanagement.model.enums;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    DONE;

    /**
     * Is kurali #4: Status gecisleri yalnizca
     * OPEN -> IN_PROGRESS ve IN_PROGRESS -> DONE seklinde olabilir.
     * DONE tekrar OPEN yapilamaz.
     */
    public boolean canTransitionTo(TicketStatus target) {
        if (this == OPEN && target == IN_PROGRESS) {
            return true;
        }
        return this == IN_PROGRESS && target == DONE;
    }
}
