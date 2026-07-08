package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.DashboardResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.exception.NotFoundException;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Kullanici bulunamadi: " + username));
        Map<String, Long> byStatus = new LinkedHashMap<>();

        long total;
        List<TicketResponse> lastFive;
        if (user.getRole() == Role.ADMIN) {
            total = ticketRepository.count();
            for (TicketStatus status : TicketStatus.values()) {
                byStatus.put(status.name(), ticketRepository.countByStatus(status));
            }
            lastFive = ticketRepository.findTop5ByOrderByCreatedDateDesc().stream()
                    .map(TicketResponse::from)
                    .toList();
        } else {
            total = ticketRepository.countVisibleByUserId(user.getId());
            for (TicketStatus status : TicketStatus.values()) {
                byStatus.put(status.name(), ticketRepository.countVisibleByUserIdAndStatus(user.getId(), status));
            }
            lastFive = ticketRepository.findVisibleTopByUserId(user.getId(), PageRequest.of(0, 5)).stream()
                    .map(TicketResponse::from)
                    .toList();
        }

        return new DashboardResponse(total, byStatus, lastFive);
    }
}
