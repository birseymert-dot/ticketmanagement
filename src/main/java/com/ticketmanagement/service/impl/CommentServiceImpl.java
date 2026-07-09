package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.request.CommentRequest;
import com.ticketmanagement.dto.response.CommentResponse;
import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.Comment;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              TicketRepository ticketRepository,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request, String username) {
        // Kural: yorum bos olamaz (DTO validation'a ek olarak service seviyesinde de kontrol)
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Yorum bos olamaz");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket bulunamadi: " + ticketId));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
        validateTicketAccess(ticket, author);

        Comment comment = new Comment();
        comment.setContent(request.getContent().trim());
        comment.setTicket(ticket);
        comment.setAuthor(author);

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicket(Long ticketId, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket bulunamadi: " + ticketId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
        validateTicketAccess(ticket, user);

        return commentRepository.findByTicketIdOrderByCreatedDateAsc(ticketId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    /**
     * Comment erisimi ticket goruntuleme kuraliyla ayni tutulur:
     * ADMIN tum ticket'larda (kendi ticket'lari dahil) yorum gorebilir/ekleyebilir.
     * USER sadece kendi olusturdugu ticket uzerinde yorum islemi yapabilir.
     */
    private void validateTicketAccess(Ticket ticket, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Bu ticket icin yorum yetkiniz yok");
        }
    }
}
