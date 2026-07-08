package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.request.LoginRequest;
import com.ticketmanagement.dto.request.RegisterRequest;
import com.ticketmanagement.dto.response.AuthResponse;
import com.ticketmanagement.exception.BadRequestException;
import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.AuditAction;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.UserRepository;
import com.ticketmanagement.security.JwtUtil;
import com.ticketmanagement.service.AuditLogService;
import com.ticketmanagement.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Bu kullanici adi zaten kullaniliyor");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Bu email zaten kullaniliyor");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Kullanici bulunamadi"));

        auditLogService.log(AuditAction.LOGIN, user.getUsername(), null, "Kullanici giris yapti");

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
