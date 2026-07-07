package com.ticketmanagement.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private long totalTickets;
    private Map<String, Long> ticketsByStatus;
    private List<TicketResponse> lastFiveTickets;

    public DashboardResponse() {
    }

    public DashboardResponse(long totalTickets, Map<String, Long> ticketsByStatus, List<TicketResponse> lastFiveTickets) {
        this.totalTickets = totalTickets;
        this.ticketsByStatus = ticketsByStatus;
        this.lastFiveTickets = lastFiveTickets;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Map<String, Long> getTicketsByStatus() {
        return ticketsByStatus;
    }

    public void setTicketsByStatus(Map<String, Long> ticketsByStatus) {
        this.ticketsByStatus = ticketsByStatus;
    }

    public List<TicketResponse> getLastFiveTickets() {
        return lastFiveTickets;
    }

    public void setLastFiveTickets(List<TicketResponse> lastFiveTickets) {
        this.lastFiveTickets = lastFiveTickets;
    }
}
