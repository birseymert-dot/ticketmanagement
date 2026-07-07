package com.ticketmanagement.service;

import com.ticketmanagement.dto.request.CommentRequest;
import com.ticketmanagement.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CommentRequest request, String username);

    List<CommentResponse> getCommentsByTicket(Long ticketId, String username);
}
