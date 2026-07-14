package com.ticketmanagement.repository;

import com.ticketmanagement.model.entity.Ticket;
import com.ticketmanagement.model.enums.Department;
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

    /**
     * ADMIN icin: tum ticket'lar uzerinde status / priority / assigned user filtresi + pagination.
     * searchName dolu ise olusturan/atanan kullanici adinda veya departmaninda gecen ticket'lar doner.
     */
    @Query("SELECT t FROM Ticket t LEFT JOIN t.assignedTo assignee JOIN t.createdBy creator " +
            "WHERE (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assignedToId IS NULL OR assignee.id = :assignedToId) " +
            "AND (:searchName IS NULL " +
            "     OR LOWER(creator.username) LIKE LOWER(CONCAT('%', :searchName, '%')) " +
            "     OR LOWER(assignee.username) LIKE LOWER(CONCAT('%', :searchName, '%')) " +
            "     OR (:searchDepartment IS NOT NULL AND (creator.department = :searchDepartment OR assignee.department = :searchDepartment)))")
    Page<Ticket> findAllWithFilters(@Param("status") TicketStatus status,
                                    @Param("priority") TicketPriority priority,
                                    @Param("assignedToId") Long assignedToId,
                                    @Param("searchName") String searchName,
                                    @Param("searchDepartment") Department searchDepartment,
                                    Pageable pageable);

    /**
     * USER ve ADMIN alt bolumleri icin: kendi olusturdugu veya kendisine atanan ticket'lar (+ ayni filtreler).
     * view=ASSIGNED sadece atananlari, view=CREATED sadece olusturdugu ticket'lari listeler.
     */
    @Query("SELECT t FROM Ticket t LEFT JOIN t.assignedTo assignee JOIN t.createdBy creator " +
            "WHERE ((:view = 'ASSIGNED' AND assignee.id = :userId) " +
            "OR (:view = 'CREATED' AND creator.id = :userId) " +
            "OR ((:view IS NULL OR :view = 'ALL') AND (creator.id = :userId OR assignee.id = :userId))) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assignedToId IS NULL OR assignee.id = :assignedToId) " +
            "AND (:searchName IS NULL " +
            "     OR LOWER(creator.username) LIKE LOWER(CONCAT('%', :searchName, '%')) " +
            "     OR LOWER(assignee.username) LIKE LOWER(CONCAT('%', :searchName, '%')) " +
            "     OR (:searchDepartment IS NOT NULL AND (creator.department = :searchDepartment OR assignee.department = :searchDepartment)))")
    Page<Ticket> findOwnWithFilters(@Param("userId") Long userId,
                                    @Param("view") String view,
                                    @Param("status") TicketStatus status,
                                    @Param("priority") TicketPriority priority,
                                    @Param("assignedToId") Long assignedToId,
                                    @Param("searchName") String searchName,
                                    @Param("searchDepartment") Department searchDepartment,
                                    Pageable pageable);

    long countByStatus(TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdBy.id = :userId OR t.assignedTo.id = :userId")
    long countVisibleByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t " +
            "WHERE (t.createdBy.id = :userId OR t.assignedTo.id = :userId) " +
            "AND t.status = :status")
    long countVisibleByUserIdAndStatus(@Param("userId") Long userId,
                                       @Param("status") TicketStatus status);

    long countByCreatedById(Long userId);

    long countByAssignedToId(Long userId);

    List<Ticket> findByCreatedById(Long userId);

    List<Ticket> findByAssignedToId(Long userId);

    List<Ticket> findTop8ByOrderByCreatedDateDesc();

    List<Ticket> findAllByOrderByCreatedDateDesc();

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.createdBy.id = :userId OR t.assignedTo.id = :userId " +
            "ORDER BY t.createdDate DESC")
    List<Ticket> findVisibleTopByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.createdBy.id = :userId OR t.assignedTo.id = :userId " +
            "ORDER BY t.createdDate DESC")
    List<Ticket> findVisibleByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);
}
