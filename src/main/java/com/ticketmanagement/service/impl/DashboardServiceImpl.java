package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.DashboardResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {
        return getDashboard(username, null);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username, LocalDate reportDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
        List<Ticket> tickets = user.getRole() == Role.ADMIN
                ? ticketRepository.findAllByOrderByCreatedDateDesc()
                : ticketRepository.findVisibleByUserIdOrderByCreatedDateDesc(user.getId());

        LocalDate today = LocalDate.now();
        LocalDate selectedDate = reportDate != null ? reportDate : today;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L).atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7);
        LocalDateTime reportStart = selectedDate.atStartOfDay();
        LocalDateTime reportEnd = selectedDate.plusDays(1).atStartOfDay();

        List<Ticket> reportCreatedTickets = tickets.stream()
                .filter(ticket -> isBetween(ticket.getCreatedDate(), reportStart, reportEnd))
                .toList();

        DashboardResponse response = new DashboardResponse();
        response.setReportDate(selectedDate.toString());
        response.setWeekStartDate(weekStart.toLocalDate().toString());
        response.setWeekEndDate(weekEnd.toLocalDate().minusDays(1).toString());
        response.setTotalTickets(tickets.size());
        response.setTicketsByStatus(statusCounts(tickets));
        response.setTicketsByStatusThisWeek(statusCounts(tickets.stream()
                .filter(ticket -> isOnOrAfter(ticket.getCreatedDate(), weekStart))
                .toList()));
        response.setOpenTicketsByPriority(openPriorityCounts(tickets));
        response.setReportTicketsByStatus(statusCounts(reportCreatedTickets));
        response.setReportTicketsByPriority(priorityCounts(reportCreatedTickets));
        response.setReportOpenTicketsByPriority(openPriorityCounts(reportCreatedTickets));
        response.setReportAssignees(assigneeCounts(reportCreatedTickets));
        response.setLastFiveTickets(tickets.stream()
                .limit(8)
                .map(TicketResponse::from)
                .toList());
        response.setReportOpenedTickets(reportCreatedTickets.size());
        response.setReportSolvedTickets(countSolvedBetween(tickets, reportStart, reportEnd));
        response.setReportOverdueTickets(countExpiredBetween(tickets, reportStart, reportEnd, now));
        response.setReportUnresolvedTickets(reportCreatedTickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.DONE)
                .count());
        response.setNewTicketsToday(countCreatedBetween(tickets, todayStart, tomorrowStart));
        response.setOpenedThisWeek(tickets.stream()
                .filter(ticket -> isOnOrAfter(ticket.getCreatedDate(), weekStart))
                .count());
        response.setSolvedThisWeek(tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.DONE)
                .filter(ticket -> isOnOrAfter(ticket.getUpdatedDate(), weekStart))
                .count());
        response.setExpiredTicketsThisWeek(countExpiredBetween(tickets, weekStart, weekEnd, now));
        response.setUnresolvedTickets(tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.DONE)
                .count());
        response.setUnassignedTickets(tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() == null)
                .filter(ticket -> ticket.getStatus() != TicketStatus.DONE)
                .count());
        response.setOverdueTickets(tickets.stream()
                .filter(ticket -> isExpired(ticket, now))
                .count());
        response.setExpiredTicketsToday(tickets.stream()
                .filter(ticket -> isExpired(ticket, now))
                .filter(ticket -> {
                    LocalDateTime expiresAt = expiresAt(ticket);
                    return expiresAt != null && expiresAt.toLocalDate().equals(today);
                })
                .count());
        response.setTopSolversReportDate(topSolversBetween(tickets, reportStart, reportEnd));
        response.setTopSolversThisWeek(topSolversThisWeek(tickets, weekStart));
        response.setWeeklyVolume(weeklyVolume(tickets, today));

        return response;
    }

    private Map<String, Long> statusCounts(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            counts.put(status.name(), 0L);
        }
        for (Ticket ticket : tickets) {
            counts.put(ticket.getStatus().name(), counts.get(ticket.getStatus().name()) + 1L);
        }
        return counts;
    }

    private Map<String, Long> openPriorityCounts(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TicketPriority priority : TicketPriority.values()) {
            counts.put(priority.name(), 0L);
        }
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.DONE) {
                counts.put(ticket.getPriority().name(), counts.get(ticket.getPriority().name()) + 1L);
            }
        }
        return counts;
    }

    private Map<String, Long> priorityCounts(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TicketPriority priority : TicketPriority.values()) {
            counts.put(priority.name(), 0L);
        }
        for (Ticket ticket : tickets) {
            counts.put(ticket.getPriority().name(), counts.get(ticket.getPriority().name()) + 1L);
        }
        return counts;
    }

    private List<DashboardResponse.AssigneeStat> assigneeCounts(List<Ticket> tickets) {
        Map<Long, Long> counts = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        long unassignedCount = 0L;

        for (Ticket ticket : tickets) {
            User assignee = ticket.getAssignedTo();
            if (assignee == null) {
                unassignedCount++;
                continue;
            }
            counts.merge(assignee.getId(), 1L, Long::sum);
            users.put(assignee.getId(), assignee);
        }

        List<DashboardResponse.AssigneeStat> stats = counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    User assignee = users.get(entry.getKey());
                    return new DashboardResponse.AssigneeStat(
                            assignee.getUsername(),
                            assignee.getRole().name(),
                            assignee.getDepartment() != null ? assignee.getDepartment().name() : null,
                            assignee.getProfileImage(),
                            entry.getValue()
                    );
                })
                .toList();

        if (unassignedCount == 0L) {
            return stats;
        }

        java.util.ArrayList<DashboardResponse.AssigneeStat> withUnassigned = new java.util.ArrayList<>(stats);
        withUnassigned.add(new DashboardResponse.AssigneeStat("Atama yok", null, null, null, unassignedCount));
        return withUnassigned;
    }

    private long countCreatedBetween(List<Ticket> tickets, LocalDateTime start, LocalDateTime end) {
        return tickets.stream()
                .filter(ticket -> isBetween(ticket.getCreatedDate(), start, end))
                .count();
    }

    private boolean isOnOrAfter(LocalDateTime value, LocalDateTime start) {
        return value != null && !value.isBefore(start);
    }

    private boolean isBetween(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        return value != null && !value.isBefore(start) && value.isBefore(end);
    }

    private long countSolvedBetween(List<Ticket> tickets, LocalDateTime start, LocalDateTime end) {
        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.DONE)
                .filter(ticket -> isBetween(ticket.getUpdatedDate(), start, end))
                .count();
    }

    private long countExpiredBetween(List<Ticket> tickets, LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        return tickets.stream()
                .filter(ticket -> isExpired(ticket, now))
                .filter(ticket -> isBetween(expiresAt(ticket), start, end))
                .count();
    }

    private boolean isExpired(Ticket ticket, LocalDateTime now) {
        LocalDateTime expiresAt = expiresAt(ticket);
        return ticket.getStatus() != TicketStatus.DONE
                && expiresAt != null
                && !expiresAt.isAfter(now);
    }

    private LocalDateTime expiresAt(Ticket ticket) {
        if (ticket.getExpiresAt() != null) {
            return ticket.getExpiresAt();
        }
        if (ticket.getCreatedDate() == null || ticket.getPriority() == null) {
            return null;
        }
        return ticket.getCreatedDate().plusHours(slaHours(ticket.getPriority()));
    }

    private long slaHours(TicketPriority priority) {
        return switch (priority) {
            case HIGH -> 4L;
            case MEDIUM -> 24L;
            case LOW -> 72L;
        };
    }

    private List<DashboardResponse.SolverStat> topSolversThisWeek(List<Ticket> tickets, LocalDateTime weekStart) {
        return topSolversBetween(tickets, weekStart, LocalDateTime.now().plusDays(1));
    }

    private List<DashboardResponse.SolverStat> topSolversBetween(List<Ticket> tickets,
                                                                 LocalDateTime start,
                                                                 LocalDateTime end) {
        Map<Long, Long> solvedCounts = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.DONE
                    || !isBetween(ticket.getUpdatedDate(), start, end)
                    || ticket.getAssignedTo() == null) {
                continue;
            }
            User assignee = ticket.getAssignedTo();
            solvedCounts.merge(assignee.getId(), 1L, Long::sum);
            users.put(assignee.getId(), assignee);
        }

        return solvedCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> {
                    User solver = users.get(entry.getKey());
                    return new DashboardResponse.SolverStat(
                            solver.getUsername(),
                            solver.getRole().name(),
                            solver.getDepartment() != null ? solver.getDepartment().name() : null,
                            solver.getProfileImage(),
                            entry.getValue()
                    );
                })
                .toList();
    }

    private List<DashboardResponse.DailyTicketStat> weeklyVolume(List<Ticket> tickets, LocalDate today) {
        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(offset -> today.minusDays(6L - offset))
                .map(day -> new DashboardResponse.DailyTicketStat(
                        day.getDayOfMonth() + "." + String.format("%02d", day.getMonthValue()),
                        tickets.stream()
                                .filter(ticket -> ticket.getCreatedDate() != null)
                                .filter(ticket -> ticket.getCreatedDate().toLocalDate().equals(day))
                                .count(),
                        tickets.stream()
                                .filter(ticket -> ticket.getStatus() == TicketStatus.DONE)
                                .filter(ticket -> ticket.getUpdatedDate() != null)
                                .filter(ticket -> ticket.getUpdatedDate().toLocalDate().equals(day))
                                .count()
                ))
                .toList();
    }
}
