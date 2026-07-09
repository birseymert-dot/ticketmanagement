package com.ticketmanagement.controller;

import com.ticketmanagement.dto.request.StatusUpdateRequest;
import com.ticketmanagement.dto.request.TicketCreateRequest;
import com.ticketmanagement.dto.request.TicketUpdateRequest;
import com.ticketmanagement.dto.response.PageResponse;
import com.ticketmanagement.dto.response.TicketResponse;
import com.ticketmanagement.model.enums.TicketPriority;
import com.ticketmanagement.model.enums.TicketStatus;
import com.ticketmanagement.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request,
                                                       Authentication authentication) {
        TicketResponse response = ticketService.createTicket(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listeleme: pagination zorunlu; status / priority / assignedTo filtreleri ve
     * kullanici adina gore arama (searchUser: olusturan veya atanan) opsiyonel.
     */
    @GetMapping
    public ResponseEntity<PageResponse<TicketResponse>> getTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String searchUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(
                ticketService.getTickets(status, priority, assignedToId, searchUser, pageable, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketById(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id,
                                                       @Valid @RequestBody TicketUpdateRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody StatusUpdateRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id, Authentication authentication) {
        ticketService.deleteTicket(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
