package com.ticketmanagement.service;

import com.ticketmanagement.dto.request.StatusUpdateRequest;
import com.ticketmanagement.dto.request.TicketCreateRequest;
import com.ticketmanagement.dto.request.TicketUpdateRequest;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Is kurallarini dogrulayan unit testler:
 * - Ticket olusturma (status otomatik OPEN)
 * - Status gecis kurali (OPEN -> IN_PROGRESS -> DONE, DONE geri donemez)
 * - Yetkisiz guncelleme engellenir (sadece olusturan guncelleyebilir)
 * - Silme yetki kontrolu (sadece ADMIN silebilir)
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User creator;
    private User assignee;
    private User otherUser;
    private User admin;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        creator = buildUser(1L, "creator", Role.USER);
        assignee = buildUser(2L, "assignee", Role.USER);
        otherUser = buildUser(3L, "other", Role.USER);
        admin = buildUser(4L, "admin", Role.ADMIN);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Test aciklamasi");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCreatedBy(creator);
        ticket.setAssignedTo(assignee);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setUpdatedDate(LocalDateTime.now());
    }

    private User buildUser(Long id, String username, Role role) {
        User user = new User(username, username + "@test.com", "hashed", role);
        user.setId(id);
        return user;
    }

    // ------------------------------------------------------------------
    // 1) TICKET OLUSTURMA
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Ticket olusturuldugunda status otomatik OPEN olmalidir")
    void createTicket_shouldSetStatusToOpenAutomatically() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("Yeni Ticket");
        request.setDescription("Aciklama");
        request.setPriority(TicketPriority.HIGH);

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(99L);
            t.setCreatedDate(LocalDateTime.now());
            t.setUpdatedDate(LocalDateTime.now());
            return t;
        });

        TicketResponse response = ticketService.createTicket(request, "creator");

        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals("Yeni Ticket", response.getTitle());
        assertEquals("creator", response.getCreatedBy());
        assertNotNull(response.getExpiresAt());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertEquals(TicketStatus.OPEN, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getExpiresAt());
    }

    // ------------------------------------------------------------------
    // 2) STATUS GECIS KURALI
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ADMIN OPEN -> IN_PROGRESS gecisini yapabilmelidir")
    void updateStatus_shouldAllowValidTransition() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.updateStatus(10L, request, "admin");

        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    @DisplayName("DONE olan ticket tekrar OPEN yapilamamalidir")
    void updateStatus_shouldRejectDoneToOpenTransition() {
        ticket.setStatus(TicketStatus.DONE);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.OPEN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketService.updateStatus(10L, request, "admin"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("OPEN -> DONE gecisi (ara adim atlanarak) engellenmelidir")
    void updateStatus_shouldRejectSkippingIntermediateStep() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.DONE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketService.updateStatus(10L, request, "admin"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Neden belirtilmeden ticket beklemeye alinamaz")
    void updateStatus_shouldRejectHoldWithoutReason() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.HOLD);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketService.updateStatus(10L, request, "admin"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Neden belirtilerek beklemeye alinabilir; neden ticket'ta saklanir")
    void updateStatus_shouldAllowHoldWithReason() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.HOLD);
        request.setReason("Tedarikciden donus bekleniyor");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.updateStatus(10L, request, "admin");

        assertEquals(TicketStatus.HOLD, response.getStatus());
        assertEquals("Tedarikciden donus bekleniyor", response.getHoldReason());
    }

    @Test
    @DisplayName("Beklemeden cikan ticket OPEN'a doner ve neden temizlenir")
    void updateStatus_shouldReturnToOpenAndClearReasonWhenLeavingHold() {
        ticket.setStatus(TicketStatus.HOLD);
        ticket.setHoldReason("Eski neden");
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.OPEN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.updateStatus(10L, request, "admin");

        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertNull(response.getHoldReason());
    }

    @Test
    @DisplayName("Beklemedeki ticket dogrudan isleme alinamaz (HOLD -> IN_PROGRESS yasak)")
    void updateStatus_shouldRejectProcessingWhileOnHold() {
        ticket.setStatus(TicketStatus.HOLD);
        ticket.setHoldReason("Beklemede");
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketService.updateStatus(10L, request, "admin"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Ticket'i olusturan USER kendi ticket'ini beklemeye alabilir")
    void updateStatus_shouldAllowCreatorToHoldOwnTicket() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.HOLD);
        request.setReason("Konu netlesene kadar bekletiyorum");

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.updateStatus(10L, request, "creator");

        assertEquals(TicketStatus.HOLD, response.getStatus());
        assertEquals("Konu netlesene kadar bekletiyorum", response.getHoldReason());
    }

    @Test
    @DisplayName("Olusturan USER bile ticket'i isleme alamaz (sadece ADMIN)")
    void updateStatus_shouldRejectCreatorProcessing() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class,
                () -> ticketService.updateStatus(10L, request, "creator"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Baskasinin ticket'ini USER beklemeye alamaz")
    void updateStatus_shouldRejectNonCreatorHold() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.HOLD);
        request.setReason("neden");

        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class,
                () -> ticketService.updateStatus(10L, request, "other"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("ADMIN olmayan kullanici (atanan dahil) status degistirememelidir")
    void updateStatus_shouldRejectNonAdmin() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        // Ticket'a atanan kullanici bile artik status degistiremez
        when(userRepository.findByUsername("assignee")).thenReturn(Optional.of(assignee));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class,
                () -> ticketService.updateStatus(10L, request, "assignee"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // ------------------------------------------------------------------
    // 3) YETKISIZ GUNCELLEME
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Ticket'i olusturmayan kullanicinin guncelleme girisimi engellenmelidir")
    void updateTicket_shouldRejectNonCreator() {
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setTitle("Degistirilmis Title");
        request.setDescription("Degistirilmis aciklama");
        request.setPriority(TicketPriority.LOW);

        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class,
                () -> ticketService.updateTicket(10L, request, "other"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Ticket'i olusturan kullanici guncelleyebilmelidir")
    void updateTicket_shouldAllowCreator() {
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setTitle("Guncel Title");
        request.setDescription("Guncel aciklama");
        request.setPriority(TicketPriority.LOW);

        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse response = ticketService.updateTicket(10L, request, "creator");

        assertEquals("Guncel Title", response.getTitle());
        assertEquals(TicketPriority.LOW, response.getPriority());
        // Is kurali #6: createdBy degismemis olmali
        assertEquals("creator", response.getCreatedBy());
    }

    // ------------------------------------------------------------------
    // 4) SILME YETKI KONTROLU
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ADMIN olmayan kullanici ticket silememelidir")
    void deleteTicket_shouldRejectNonAdmin() {
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));

        assertThrows(ForbiddenException.class,
                () -> ticketService.deleteTicket(10L, "creator"));
        verify(ticketRepository, never()).delete(any(Ticket.class));
    }

    @Test
    @DisplayName("ADMIN ticket silebilmelidir")
    void deleteTicket_shouldAllowAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(10L, "admin");

        verify(ticketRepository).delete(ticket);
    }
}
