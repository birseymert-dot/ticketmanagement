package com.ticketmanagement.service;

import com.ticketmanagement.dto.request.StatusUpdateRequest;
import com.ticketmanagement.dto.request.TicketCreateRequest;
import com.ticketmanagement.dto.request.TicketUpdateRequest;
import com.ticketmanagement.dto.response.PageResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketResponse createTicket(TicketCreateRequest request, String username);

    PageResponse<TicketResponse> getTickets(TicketStatus status,
                                            TicketPriority priority,
                                            Long assignedToId,
                                            String searchName,
                                            String view,
                                            Pageable pageable,
                                            String username);

    TicketResponse getTicketById(Long id, String username);

    TicketResponse updateTicket(Long id, TicketUpdateRequest request, String username);

    TicketResponse updateStatus(Long id, StatusUpdateRequest request, String username);

    void deleteTicket(Long id, String username);
}
