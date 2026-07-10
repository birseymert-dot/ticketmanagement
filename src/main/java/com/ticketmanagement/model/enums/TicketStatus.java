package com.ticketmanagement.model.enums;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    HOLD,
    DONE;

    /**
     * Status gecis kurallari:
     * - OPEN        -> IN_PROGRESS veya HOLD
     * - IN_PROGRESS -> DONE veya HOLD
     * - HOLD        -> SADECE OPEN (beklemeden cikan ticket, islemde olsa bile
     *                  "Acik"a doner; admin yeniden isleme almalidir. Beklemedeki
     *                  ticket dogrudan islese sokulamaz.)
     * - DONE        -> gecis yok (tekrar acilamaz)
     * HOLD'a gecis icin neden zorunludur (service katmaninda kontrol edilir).
     */
    public boolean canTransitionTo(TicketStatus target) {
        return switch (this) {
            case OPEN -> target == IN_PROGRESS || target == HOLD;
            case IN_PROGRESS -> target == DONE || target == HOLD;
            case HOLD -> target == OPEN;
            case DONE -> false;
        };
    }
}
