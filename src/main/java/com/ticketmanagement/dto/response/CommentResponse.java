package com.ticketmanagement.dto.response;

import com.ticketmanagement.model.entity.Comment;
import com.ticketmanagement.model.enums.Department;
import com.ticketmanagement.model.enums.Role;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String content;
    private String author;
    private Role authorRole;
    private Department authorDepartment;
    private String authorProfileImage;
    private Long ticketId;
    private LocalDateTime createdDate;

    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.content = comment.getContent();
        response.author = comment.getAuthor().getUsername();
        response.authorRole = comment.getAuthor().getRole();
        response.authorDepartment = comment.getAuthor().getDepartment();
        response.authorProfileImage = comment.getAuthor().getProfileImage();
        response.ticketId = comment.getTicket().getId();
        response.createdDate = comment.getCreatedDate();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public Role getAuthorRole() {
        return authorRole;
    }

    public Department getAuthorDepartment() {
        return authorDepartment;
    }

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
