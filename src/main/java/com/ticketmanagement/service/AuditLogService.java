package com.ticketmanagement.service;

import com.ticketmanagement.dto.response.AuditLogResponse;
import com.ticketmanagement.model.enums.AuditAction;

import java.util.List;

public interface AuditLogService {

    void log(AuditAction action, String username, Long ticketId, String details);

    List<AuditLogResponse> getAllLogs();
}
