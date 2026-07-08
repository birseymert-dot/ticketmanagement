package com.ticketmanagement.repository;

import com.ticketmanagement.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketIdOrderByCreatedDateAsc(Long ticketId);

    void deleteByTicketId(Long ticketId);

    void deleteByAuthorId(Long authorId);

    long countByAuthorId(Long userId);
}
