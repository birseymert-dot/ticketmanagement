package com.ticketmanagement.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private long totalTickets;
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByStatusThisWeek;
    private Map<String, Long> openTicketsByPriority;
    private Map<String, Long> reportTicketsByStatus;
    private Map<String, Long> reportTicketsByPriority;
    private Map<String, Long> reportOpenTicketsByPriority;
    private List<TicketResponse> lastFiveTickets;
    private String reportDate;
    private String weekStartDate;
    private String weekEndDate;
    private long reportOpenedTickets;
    private long reportSolvedTickets;
    private long reportOverdueTickets;
    private long reportUnresolvedTickets;
    private long newTicketsToday;
    private long expiredTicketsToday;
    private long expiredTicketsThisWeek;
    private long overdueTickets;
    private long openedThisWeek;
    private long solvedThisWeek;
    private long unresolvedTickets;
    private long unassignedTickets;
    private List<AssigneeStat> reportAssignees;
    private List<SolverStat> topSolversReportDate;
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

    public Map<String, Long> getReportTicketsByStatus() {
        return reportTicketsByStatus;
    }

    public void setReportTicketsByStatus(Map<String, Long> reportTicketsByStatus) {
        this.reportTicketsByStatus = reportTicketsByStatus;
    }

    public Map<String, Long> getReportOpenTicketsByPriority() {
        return reportOpenTicketsByPriority;
    }

    public Map<String, Long> getReportTicketsByPriority() {
        return reportTicketsByPriority;
    }

    public void setReportTicketsByPriority(Map<String, Long> reportTicketsByPriority) {
        this.reportTicketsByPriority = reportTicketsByPriority;
    }

    public void setReportOpenTicketsByPriority(Map<String, Long> reportOpenTicketsByPriority) {
        this.reportOpenTicketsByPriority = reportOpenTicketsByPriority;
    }

    public List<TicketResponse> getLastFiveTickets() {
        return lastFiveTickets;
    }

    public void setLastFiveTickets(List<TicketResponse> lastFiveTickets) {
        this.lastFiveTickets = lastFiveTickets;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(String weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public String getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(String weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public long getReportOpenedTickets() {
        return reportOpenedTickets;
    }

    public void setReportOpenedTickets(long reportOpenedTickets) {
        this.reportOpenedTickets = reportOpenedTickets;
    }

    public long getReportSolvedTickets() {
        return reportSolvedTickets;
    }

    public void setReportSolvedTickets(long reportSolvedTickets) {
        this.reportSolvedTickets = reportSolvedTickets;
    }

    public long getReportOverdueTickets() {
        return reportOverdueTickets;
    }

    public void setReportOverdueTickets(long reportOverdueTickets) {
        this.reportOverdueTickets = reportOverdueTickets;
    }

    public long getReportUnresolvedTickets() {
        return reportUnresolvedTickets;
    }

    public void setReportUnresolvedTickets(long reportUnresolvedTickets) {
        this.reportUnresolvedTickets = reportUnresolvedTickets;
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

    public long getExpiredTicketsThisWeek() {
        return expiredTicketsThisWeek;
    }

    public void setExpiredTicketsThisWeek(long expiredTicketsThisWeek) {
        this.expiredTicketsThisWeek = expiredTicketsThisWeek;
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

    public List<AssigneeStat> getReportAssignees() {
        return reportAssignees;
    }

    public void setReportAssignees(List<AssigneeStat> reportAssignees) {
        this.reportAssignees = reportAssignees;
    }

    public List<SolverStat> getTopSolversReportDate() {
        return topSolversReportDate;
    }

    public void setTopSolversReportDate(List<SolverStat> topSolversReportDate) {
        this.topSolversReportDate = topSolversReportDate;
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

    public static class AssigneeStat {
        private String username;
        private String role;
        private String department;
        private String profileImage;
        private long ticketCount;

        public AssigneeStat() {
        }

        public AssigneeStat(String username, String role, String department, String profileImage, long ticketCount) {
            this.username = username;
            this.role = role;
            this.department = department;
            this.profileImage = profileImage;
            this.ticketCount = ticketCount;
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

        public long getTicketCount() {
            return ticketCount;
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
