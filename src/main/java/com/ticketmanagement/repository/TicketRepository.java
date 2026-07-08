package com.ticketmanagement.repository;

import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** ADMIN icin: tum ticket'lar uzerinde status / priority / assigned user filtresi + pagination. */
    @Query("SELECT t FROM Ticket t " +
            "WHERE (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Ticket> findAllWithFilters(@Param("status") TicketStatus status,
                                    @Param("priority") TicketPriority priority,
                                    @Param("assignedToId") Long assignedToId,
                                    Pageable pageable);

    /** USER icin: sadece kendi olusturdugu veya kendisine atanan ticket'lar. */
    @Query("SELECT t FROM Ticket t " +
            "WHERE (t.createdBy.id = :userId OR t.assignedTo.id = :userId) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Ticket> findOwnWithFilters(@Param("userId") Long userId,
                                    @Param("status") TicketStatus status,
                                    @Param("priority") TicketPriority priority,
                                    @Param("assignedToId") Long assignedToId,
                                    Pageable pageable);

    long countByStatus(TicketStatus status);

    long countByCreatedById(Long userId);

    long countByAssignedToId(Long userId);

    List<Ticket> findTop5ByOrderByCreatedDateDesc();
}
