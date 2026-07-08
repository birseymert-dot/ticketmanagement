package com.ticketmanagement.service;

import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.exception.ForbiddenException;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kullanici silme is kurallarini dogrulayan testler:
 * - Sadece ADMIN kullanici silebilir.
 * - Admin kendi hesabini silemez.
 * - Silinen kullanicinin ticket ve yorumlari temizlenir,
 *   atanmis oldugu ticket'lar atamasiz kalir.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private User admin;
    private User normalUser;
    private User target;

    @BeforeEach
    void setUp() {
        admin = buildUser(1L, "admin", Role.ADMIN);
        normalUser = buildUser(2L, "kullanici", Role.USER);
        target = buildUser(3L, "silinecek", Role.USER);
    }

    private User buildUser(Long id, String username, Role role) {
        User user = new User(username, username + "@test.com", "hash", role);
        user.setId(id);
        return user;
    }

    @Test
    @DisplayName("ADMIN olmayan kullanici, kullanici silememelidir")
    void deleteUser_shouldRejectNonAdmin() {
        when(userRepository.findByUsername("kullanici")).thenReturn(Optional.of(normalUser));

        assertThrows(ForbiddenException.class,
                () -> userService.deleteUser(3L, "kullanici"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Admin kendi hesabini silememelidir")
    void deleteUser_shouldRejectSelfDelete() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class,
                () -> userService.deleteUser(1L, "admin"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Olmayan kullanici silinmek istenirse 404 donmelidir")
    void deleteUser_shouldRejectUnknownTarget() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(99L, "admin"));
    }

    @Test
    @DisplayName("ADMIN kullaniciyi silebilmeli; ticket/yorum temizligi yapilmalidir")
    void deleteUser_shouldAllowAdminAndCleanUpData() {
        Ticket created = new Ticket();
        created.setId(10L);
        created.setCreatedBy(target);

        Ticket assigned = new Ticket();
        assigned.setId(11L);
        assigned.setCreatedBy(admin);
        assigned.setAssignedTo(target);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(target));
        when(ticketRepository.findByCreatedById(3L)).thenReturn(List.of(created));
        when(ticketRepository.findByAssignedToId(3L)).thenReturn(List.of(assigned));

        userService.deleteUser(3L, "admin");

        verify(commentRepository).deleteByAuthorId(3L);       // yazdigi yorumlar
        verify(commentRepository).deleteByTicketId(10L);      // actigi ticket'in yorumlari
        verify(ticketRepository).delete(created);             // actigi ticket
        verify(ticketRepository).save(assigned);              // atama kaldirilir
        assertNull(assigned.getAssignedTo());
        verify(userRepository).delete(target);                // kullanici silinir
    }
}
