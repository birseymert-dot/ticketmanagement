package com.ticketmanagement.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private long totalTickets;
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByStatusThisWeek;
    private Map<String, Long> openTicketsByPriority;
    private List<TicketResponse> lastFiveTickets;
    private long newTicketsToday;
    private long expiredTicketsToday;
    private long overdueTickets;
    private long openedThisWeek;
    private long solvedThisWeek;
    private long unresolvedTickets;
    private long unassignedTickets;
    private List<SolverStat> topSolversThisWeek;
    private List<DailyTicketStat> weeklyVolume;

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

    public Map<String, Long> getTicketsByStatusThisWeek() {
        return ticketsByStatusThisWeek;
    }

    public void setTicketsByStatusThisWeek(Map<String, Long> ticketsByStatusThisWeek) {
        this.ticketsByStatusThisWeek = ticketsByStatusThisWeek;
    }

    public Map<String, Long> getOpenTicketsByPriority() {
        return openTicketsByPriority;
    }

    public void setOpenTicketsByPriority(Map<String, Long> openTicketsByPriority) {
        this.openTicketsByPriority = openTicketsByPriority;
    }

    public List<TicketResponse> getLastFiveTickets() {
        return lastFiveTickets;
    }

    public void setLastFiveTickets(List<TicketResponse> lastFiveTickets) {
        this.lastFiveTickets = lastFiveTickets;
    }

    public long getNewTicketsToday() {
        return newTicketsToday;
    }

    public void setNewTicketsToday(long newTicketsToday) {
        this.newTicketsToday = newTicketsToday;
    }

    public long getExpiredTicketsToday() {
        return expiredTicketsToday;
    }

    public void setExpiredTicketsToday(long expiredTicketsToday) {
        this.expiredTicketsToday = expiredTicketsToday;
    }

    public long getOverdueTickets() {
        return overdueTickets;
    }

    public void setOverdueTickets(long overdueTickets) {
        this.overdueTickets = overdueTickets;
    }

    public long getOpenedThisWeek() {
        return openedThisWeek;
    }

    public void setOpenedThisWeek(long openedThisWeek) {
        this.openedThisWeek = openedThisWeek;
    }

    public long getSolvedThisWeek() {
        return solvedThisWeek;
    }

    public void setSolvedThisWeek(long solvedThisWeek) {
        this.solvedThisWeek = solvedThisWeek;
    }

    public long getUnresolvedTickets() {
        return unresolvedTickets;
    }

    public void setUnresolvedTickets(long unresolvedTickets) {
        this.unresolvedTickets = unresolvedTickets;
    }

    public long getUnassignedTickets() {
        return unassignedTickets;
    }

    public void setUnassignedTickets(long unassignedTickets) {
        this.unassignedTickets = unassignedTickets;
    }

    public List<SolverStat> getTopSolversThisWeek() {
        return topSolversThisWeek;
    }

    public void setTopSolversThisWeek(List<SolverStat> topSolversThisWeek) {
        this.topSolversThisWeek = topSolversThisWeek;
    }

    public List<DailyTicketStat> getWeeklyVolume() {
        return weeklyVolume;
    }

    public void setWeeklyVolume(List<DailyTicketStat> weeklyVolume) {
        this.weeklyVolume = weeklyVolume;
    }

    public static class SolverStat {
        private String username;
        private String role;
        private String department;
        private String profileImage;
        private long solvedCount;

        public SolverStat() {
        }

        public SolverStat(String username, String role, String department, String profileImage, long solvedCount) {
            this.username = username;
            this.role = role;
            this.department = department;
            this.profileImage = profileImage;
            this.solvedCount = solvedCount;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public String getDepartment() {
            return department;
        }

        public String getProfileImage() {
            return profileImage;
        }

        public long getSolvedCount() {
            return solvedCount;
        }
    }

    public static class DailyTicketStat {
        private String label;
        private long opened;
        private long solved;

        public DailyTicketStat() {
        }

        public DailyTicketStat(String label, long opened, long solved) {
            this.label = label;
            this.opened = opened;
            this.solved = solved;
        }

        public String getLabel() {
            return label;
        }

        public long getOpened() {
            return opened;
        }

        public long getSolved() {
            return solved;
        }
    }
}
