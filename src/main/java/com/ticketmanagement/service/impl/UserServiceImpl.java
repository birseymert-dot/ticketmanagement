package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.UserResponse;
import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.AuditLogService;
import com.ticketmanagement.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                           TicketRepository ticketRepository,
                           CommentRepository commentRepository,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.auditLogService = auditLogService;
    }

    /** ADMIN icin detayli kullanici listesi: departman, ticket ve yorum istatistikleriyle. */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .map(user -> UserResponse.detailed(
                        user,
                        ticketRepository.countByCreatedById(user.getId()),
                        ticketRepository.countByAssignedToId(user.getId()),
                        commentRepository.countByAuthorId(user.getId())))
                .toList();
    }

    /** Ticket atama akisi icin hafif kullanici listesi. */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignableUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * Kullanici silme (sadece ADMIN):
     * - ADMIN olmayan istekler reddedilir (403).
     * - Admin kendi hesabini silemez (400) - sistem yoneticisiz kalmasin.
     * - Silinen kullanicinin verileri temizlenir: actigi ticket'lar
     *   (yorumlariyla birlikte) silinir, baskalarinin ticket'larina yazdigi
     *   yorumlar silinir, kendisine atanmis ticket'lar atamasiz birakilir.
     * - Islem audit log'a kaydedilir.
     */
    @Override
    @Transactional
    public void deleteUser(Long id, String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + requesterUsername));

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Sadece ADMIN kullanici silebilir");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silinecek kullanici bulunamadi: " + id));

        if (target.getId().equals(requester.getId())) {
            throw new BadRequestException("Kendi hesabinizi silemezsiniz");
        }

        // 1) Kullanicinin baska ticket'lara yazdigi yorumlar
        commentRepository.deleteByAuthorId(target.getId());

        // 2) Kullanicinin actigi ticket'lar (uzerlerindeki tum yorumlarla birlikte)
        for (Ticket ticket : ticketRepository.findByCreatedById(target.getId())) {
            commentRepository.deleteByTicketId(ticket.getId());
            ticketRepository.delete(ticket);
        }

        // 3) Kullaniciya atanmis (baskalarinin actigi) ticket'lar atamasiz kalir
        for (Ticket ticket : ticketRepository.findByAssignedToId(target.getId())) {
            ticket.setAssignedTo(null);
            ticketRepository.save(ticket);
        }

        userRepository.delete(target);
        auditLogService.log(AuditAction.USER_DELETED, requesterUsername, null,
                "Kullanici silindi: " + target.getUsername());
    }
}