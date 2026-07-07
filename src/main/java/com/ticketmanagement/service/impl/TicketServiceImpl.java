package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.request.StatusUpdateRequest;
import com.ticketmanagement.dto.request.TicketCreateRequest;
import com.ticketmanagement.dto.request.TicketUpdateRequest;
import com.ticketmanagement.dto.response.PageResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.AuditLogService;
import com.ticketmanagement.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tum is kurallari bu service katmaninda uygulanir (controller'da degil):
 * 1. Ticket olusturuldugunda status otomatik OPEN olur.
 * 2. Sadece ticket'i olusturan kullanici ticket'i guncelleyebilir.
 * 3. Sadece ADMIN ticket silebilir.
 * 4. Status gecisleri: OPEN -> IN_PROGRESS -> DONE (DONE geri OPEN olamaz).
 * 5. Sadece assigned kullanici status degistirebilir.
 * 6. CreatedBy alani degistirilemez.
 */
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request, String username) {
        User creator = findUser(username);

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        // Is kurali #1: status otomatik OPEN
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(creator);

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new NotFoundException("Atanacak kullanici bulunamadi: " + request.getAssignedToId()));
            ticket.setAssignedTo(assignee);
        }

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(AuditAction.TICKET_CREATED, username, saved.getId(),
                "Ticket olusturuldu: " + saved.getTitle());
        return TicketResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getTickets(TicketStatus status,
                                                   TicketPriority priority,
                                                   Long assignedToId,
                                                   Pageable pageable,
                                                   String username) {
        User user = findUser(username);

        Page<Ticket> page;
        if (user.getRole() == Role.ADMIN) {
            // ADMIN tum ticket'lari gorebilir
            page = ticketRepository.findAllWithFilters(status, priority, assignedToId, pageable);
        } else {
            // USER sadece kendi olusturdugu veya kendisine atanan ticket'lari gorur
            page = ticketRepository.findOwnWithFilters(user.getId(), status, priority, assignedToId, pageable);
        }
        return PageResponse.from(page, TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(id);

        if (user.getRole() != Role.ADMIN && !isCreatorOrAssignee(ticket, user)) {
            throw new ForbiddenException("Bu ticket'i goruntuleme yetkiniz yok");
        }
        return TicketResponse.from(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, TicketUpdateRequest request, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(id);

        // Is kurali #2: sadece ticket'i olusturan kullanici guncelleyebilir
        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Sadece ticket'i olusturan kullanici guncelleyebilir");
        }

        // Is kurali #6: createdBy degistirilemez (DTO'da alan yok, entity'de updatable=false)
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new NotFoundException("Atanacak kullanici bulunamadi: " + request.getAssignedToId()));
            ticket.setAssignedTo(assignee);
        }

        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(AuditAction.TICKET_UPDATED, username, saved.getId(),
                "Ticket guncellendi: " + saved.getTitle());
        return TicketResponse.from(saved);
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long id, StatusUpdateRequest request, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(id);

        // Is kurali #5: sadece assigned kullanici status degistirebilir
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(user.getId())) {
            throw new ForbiddenException("Sadece ticket'a atanan kullanici status degistirebilir");
        }

        // Is kurali #4: gecerli status gecis kontrolu
        TicketStatus current = ticket.getStatus();
        TicketStatus target = request.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new BadRequestException(
                    "Gecersiz status gecisi: " + current + " -> " + target
                            + " (izin verilen gecisler: OPEN -> IN_PROGRESS, IN_PROGRESS -> DONE)");
        }

        ticket.setStatus(target);
        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(AuditAction.STATUS_CHANGED, username, saved.getId(),
                "Status degisti: " + current + " -> " + target);
        return TicketResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteTicket(Long id, String username) {
        User user = findUser(username);

        // Is kurali #3: sadece ADMIN ticket silebilir
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Sadece ADMIN ticket silebilir");
        }

        Ticket ticket = findTicket(id);
        ticketRepository.delete(ticket);
        auditLogService.log(AuditAction.TICKET_DELETED, username, id,
                "Ticket silindi: " + ticket.getTitle());
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket bulunamadi: " + id));
    }

    private boolean isCreatorOrAssignee(Ticket ticket, User user) {
        boolean isCreator = ticket.getCreatedBy().getId().equals(user.getId());
        boolean isAssignee = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(user.getId());
        return isCreator || isAssignee;
    }
}
