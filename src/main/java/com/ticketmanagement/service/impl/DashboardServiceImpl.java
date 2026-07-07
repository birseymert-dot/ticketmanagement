package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.DashboardResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.repository.TicketRepository;
import com.ticketmanagement.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long total = ticketRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            byStatus.put(status.name(), ticketRepository.countByStatus(status));
        }

        List<TicketResponse> lastFive = ticketRepository.findTop5ByOrderByCreatedDateDesc().stream()
                .map(TicketResponse::from)
                .toList();

        return new DashboardResponse(total, byStatus, lastFive);
    }
}
