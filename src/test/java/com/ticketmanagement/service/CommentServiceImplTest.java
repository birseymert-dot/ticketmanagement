package com.ticketmanagement.service;

import com.ticketmanagement.dto.request.CommentRequest;
import com.ticketmanagement.dto.response.CommentResponse;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.model.entity.Comment;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User creator;
    private User assignee;
    private User other;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        creator = buildUser(1L, "creator", Role.USER);
        assignee = buildUser(2L, "assignee", Role.USER);
        other = buildUser(3L, "other", Role.USER);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Ticket");
        ticket.setDescription("Description");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(creator);
        ticket.setAssignedTo(assignee);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setUpdatedDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Ticket'i goremeyen kullanici yorum ekleyememelidir")
    void addComment_shouldRejectUserWithoutTicketAccess() {
        CommentRequest request = new CommentRequest();
        request.setContent("Yetkisiz yorum");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

        assertThrows(ForbiddenException.class,
                () -> commentService.addComment(10L, request, "other"));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Atanan kullanici (olusturan degilse) yorum ekleyememelidir")
    void addComment_shouldRejectAssignee() {
        CommentRequest request = new CommentRequest();
        request.setContent("Inceliyorum");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("assignee")).thenReturn(Optional.of(assignee));

        assertThrows(ForbiddenException.class,
                () -> commentService.addComment(10L, request, "assignee"));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Ticket'i olusturan kullanici yorum ekleyebilmelidir")
    void addComment_shouldAllowCreator() {
        CommentRequest request = new CommentRequest();
        request.setContent("Guncelleme var mi?");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment comment = inv.getArgument(0);
            comment.setId(99L);
            comment.setCreatedDate(LocalDateTime.now());
            return comment;
        });

        CommentResponse response = commentService.addComment(10L, request, "creator");

        assertEquals("Guncelleme var mi?", response.getContent());
        assertEquals("creator", response.getAuthor());
        assertEquals(10L, response.getTicketId());
    }

    @Test
    @DisplayName("ADMIN her ticket'a (kendi ticket'i dahil) yorum ekleyebilmelidir")
    void addComment_shouldAllowAdminOnAnyTicket() {
        User admin = buildUser(4L, "admin", Role.ADMIN);
        CommentRequest request = new CommentRequest();
        request.setContent("Admin notu");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment comment = inv.getArgument(0);
            comment.setId(100L);
            comment.setCreatedDate(LocalDateTime.now());
            return comment;
        });

        CommentResponse response = commentService.addComment(10L, request, "admin");

        assertEquals("Admin notu", response.getContent());
        assertEquals("admin", response.getAuthor());
    }

    private User buildUser(Long id, String username, Role role) {
        User user = new User(username, username + "@test.com", "hashed", role);
        user.setId(id);
        return user;
    }
}

