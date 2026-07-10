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
import com.ticketmanagement.repository.CommentRepository;
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
 * 5. Status degisikligini (Isleme Al / Tamamla) sadece ADMIN yapabilir.
 *    (Dokumandaki ozgun kural "atanan kullanici" idi; proje talebiyle degistirildi.
 *    Duzenleme ve yorum yetkileri degismedi.)
 * 6. CreatedBy alani degistirilemez.
 */
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AuditLogService auditLogService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             CommentRepository commentRepository,
                             AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
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

        User assignee = resolveAssignee(request.getAssignedToId(), request.getAssignedToUsername());
        if (assignee != null) {
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
                                                   String searchName,
                                                   Pageable pageable,
                                                   String username) {
        User user = findUser(username);

        // Bos arama metni filtre uygulanmamis sayilir
        String search = (searchName == null || searchName.trim().isEmpty()) ? null : searchName.trim();

        Page<Ticket> page;
        if (user.getRole() == Role.ADMIN) {
            // ADMIN tum ticket'lari gorebilir
            page = ticketRepository.findAllWithFilters(status, priority, assignedToId, search, pageable);
        } else {
            // USER sadece kendi olusturdugu veya kendisine atanan ticket'lari gorur
            page = ticketRepository.findOwnWithFilters(user.getId(), status, priority, assignedToId, search, pageable);
        }
        return PageResponse.from(page, TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(id);

        // USER sadece kendi olusturdugu ticket'i goruntuleyebilir (ADMIN hepsini)
        if (user.getRole() != Role.ADMIN && !isCreator(ticket, user)) {
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

        User assignee = resolveAssignee(request.getAssignedToId(), request.getAssignedToUsername());
        if (assignee != null) {
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

        TicketStatus current = ticket.getStatus();
        TicketStatus target = request.getStatus();

        // Yetki kurallari:
        // - Beklemeye alma (HOLD) ve beklemeden alma (HOLD -> OPEN):
        //   ADMIN veya ticket'i olusturan kullanici yapabilir.
        // - Isleme alma (IN_PROGRESS) ve tamamlama (DONE): sadece ADMIN.
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isCreator = isCreator(ticket, user);
        boolean holdIslemi = target == TicketStatus.HOLD
                || (current == TicketStatus.HOLD && target == TicketStatus.OPEN);
        if (holdIslemi) {
            if (!isAdmin && !isCreator) {
                throw new ForbiddenException("Beklemeye alma/cikarma islemini sadece ADMIN veya ticket'i olusturan kullanici yapabilir");
            }
        } else if (!isAdmin) {
            throw new ForbiddenException("Ticket'i isleme alma ve tamamlama sadece ADMIN yetkisindedir");
        }

        // Is kurali #4: gecerli status gecis kontrolu.
        // Beklemedeki ticket dogrudan isleme sokulamaz (HOLD -> IN_PROGRESS yasak);
        // once beklemeden alinir (OPEN'a doner), admin yeniden isleme alir.
        if (!current.canTransitionTo(target)) {
            throw new BadRequestException(
                    "Gecersiz status gecisi: " + current + " -> " + target
                            + " (izin verilen gecisler: OPEN -> IN_PROGRESS/HOLD, IN_PROGRESS -> DONE/HOLD, HOLD -> OPEN)");
        }

        // HOLD kurali: beklemeye alinirken neden zorunludur ve ticket uzerinde gosterilir;
        // beklemeden cikildiginda neden temizlenir.
        String auditDetail = "Status degisti: " + current + " -> " + target;
        if (target == TicketStatus.HOLD) {
            String reason = request.getReason() == null ? "" : request.getReason().trim();
            if (reason.isEmpty()) {
                throw new BadRequestException("Ticket beklemeye alinirken neden belirtilmesi zorunludur");
            }
            ticket.setHoldReason(reason);
            auditDetail += " (Neden: " + reason + ")";
        } else {
            ticket.setHoldReason(null);
        }

        ticket.setStatus(target);
        Ticket saved = ticketRepository.save(ticket);
        auditLogService.log(AuditAction.STATUS_CHANGED, username, saved.getId(), auditDetail);
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
        // Once ticket'a bagli yorumlar silinir (foreign key kisiti nedeniyle)
        commentRepository.deleteByTicketId(id);
        ticketRepository.delete(ticket);
        auditLogService.log(AuditAction.TICKET_DELETED, username, id,
                "Ticket silindi: " + ticket.getTitle());
    }

    /**
     * Atanacak kullaniciyi ID veya kullanici adi ile bulur.
     * Ikisi de bos ise null doner (atama yapilmaz).
     */
    private User resolveAssignee(Long assignedToId, String assignedToUsername) {
        if (assignedToId != null) {
            return userRepository.findById(assignedToId)
                    .orElseThrow(() -> new NotFoundException("Atanacak kullanici bulunamadi: " + assignedToId));
        }
        if (assignedToUsername != null && !assignedToUsername.trim().isEmpty()) {
            String name = assignedToUsername.trim();
            return userRepository.findByUsernameIgnoreCase(name)
                    .orElseThrow(() -> new NotFoundException("Atanacak kullanici bulunamadi: " + name));
        }
        return null;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket bulunamadi: " + id));
    }

    private boolean isCreator(Ticket ticket, User user) {
        return ticket.getCreatedBy().getId().equals(user.getId());
    }
}
