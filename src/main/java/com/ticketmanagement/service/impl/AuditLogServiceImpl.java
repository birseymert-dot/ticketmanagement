package com.ticketmanagement.service.impl;

import com.ticketmanagement.model.entity.AuditLog;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.repository.AuditLogRepository;
import com.ticketmanagement.service.AuditLogService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void log(AuditAction action, String username, Long ticketId, String details) {
        auditLogRepository.save(new AuditLog(action, username, ticketId, details));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
