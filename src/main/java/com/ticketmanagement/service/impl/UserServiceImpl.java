package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.UserResponse;
import com.ticketmanagement.repository.CommentRepository;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public UserServiceImpl(UserRepository userRepository,
                           TicketRepository ticketRepository,
                           CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    /** ADMIN icin detayli kullanici listesi: ticket ve yorum istatistikleriyle. */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.detailed(
                        user,
                        ticketRepository.countByCreatedById(user.getId()),
                        ticketRepository.countByAssignedToId(user.getId()),
                        commentRepository.countByAuthorId(user.getId())))
                .toList();
    }
}
