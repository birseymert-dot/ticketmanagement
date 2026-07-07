package com.ticketmanagement.service;

import com.ticketmanagement.model.entity.AuditLog;
import com.ticketmanagement.model.enums.AuditAction;

import java.util.List;

public interface AuditLogService {

    void log(AuditAction action, String username, Long ticketId, String details);

    List<AuditLog> getAllLogs();
}
