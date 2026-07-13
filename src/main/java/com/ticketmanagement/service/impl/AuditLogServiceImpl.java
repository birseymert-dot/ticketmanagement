package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.response.AuditLogResponse;
import com.ticketmanagement.model.entity.AuditLog;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.repository.AuditLogRepository;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.service.AuditLogService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository,
                               UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void log(AuditAction action, String username, Long ticketId, String details) {
        auditLogRepository.save(new AuditLog(action, username, ticketId, details));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        Map<String, User> usersByUsername = userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        user -> user.getUsername().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (first, second) -> first));

        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream()
                .map(log -> AuditLogResponse.from(log, usersByUsername.get(normalize(log.getUsername()))))
                .collect(Collectors.toList());
    }

    private String normalize(String username) {
        return username == null ? "" : username.toLowerCase(Locale.ROOT);
    }
}
