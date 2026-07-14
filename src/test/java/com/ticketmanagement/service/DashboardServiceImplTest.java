package com.ticketmanagement.service;

import com.ticketmanagement.dto.response.DashboardResponse;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("USER dashboard sadece kendi gorebildigi ticket'lardan olusmalidir")
    void getDashboard_shouldFilterDataForUser() {
        User user = new User("user", "user@test.com", "hashed", Role.USER);
        user.setId(1L);
        Ticket visibleTicket = buildTicket(10L, user);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(ticketRepository.findVisibleByUserIdOrderByCreatedDateDesc(1L)).thenReturn(List.of(visibleTicket));

        DashboardResponse response = dashboardService.getDashboard("user");

        assertEquals(1L, response.getTotalTickets());
        assertEquals(1L, response.getTicketsByStatus().get("OPEN"));
        assertEquals(1L, response.getNewTicketsToday());
        assertEquals(1L, response.getOpenedThisWeek());
        assertEquals(1L, response.getUnresolvedTickets());
        assertEquals(1, response.getLastFiveTickets().size());
        assertEquals("User ticket", response.getLastFiveTickets().get(0).getTitle());
        verify(ticketRepository, never()).count();
        verify(ticketRepository, never()).findAllByOrderByCreatedDateDesc();
    }

    @Test
    @DisplayName("ADMIN dashboard tum sistem ticket verisini gormelidir")
    void getDashboard_shouldUseGlobalDataForAdmin() {
        User admin = new User("admin", "admin@test.com", "hashed", Role.ADMIN);
        admin.setId(2L);
        Ticket ticket = buildTicket(20L, admin);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findAllByOrderByCreatedDateDesc()).thenReturn(List.of(ticket));

        DashboardResponse response = dashboardService.getDashboard("admin");

        assertEquals(1L, response.getTotalTickets());
        assertEquals(1L, response.getTicketsByStatus().get("OPEN"));
        assertEquals(1L, response.getOpenTicketsByPriority().get("MEDIUM"));
        assertEquals(1L, response.getReportTicketsByStatus().get("OPEN"));
        assertEquals(1L, response.getReportTicketsByPriority().get("MEDIUM"));
        assertEquals("admin", response.getReportAssignees().get(0).getUsername());
        assertEquals(1L, response.getReportAssignees().get(0).getTicketCount());
        assertEquals(1, response.getLastFiveTickets().size());
        verify(ticketRepository, never()).countVisibleByUserId(2L);
    }

    private Ticket buildTicket(Long id, User creator) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("User ticket");
        ticket.setDescription("Description");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(creator);
        ticket.setAssignedTo(creator);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setUpdatedDate(LocalDateTime.now());
        return ticket;
    }
}
