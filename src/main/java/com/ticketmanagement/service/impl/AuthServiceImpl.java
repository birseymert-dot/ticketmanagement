package com.ticketmanagement.service.impl;

import com.ticketmanagement.dto.request.LoginRequest;
import com.ticketmanagement.dto.request.RegisterRequest;
import com.ticketmanagement.dto.response.AuthResponse;
import com.ticketmanagement.dto.response.RegisterResponse;
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

import java.util.Map;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Bilinen e-posta saglayicilarinin gecerli alan adlari.
     * "ems@gmail.cc" gibi bilinen saglayici adini tasiyan ama uzantisi
     * hatali olan adresler reddedilir. Listede olmayan alan adlari
     * (ornegin kurumsal domainler) format kontrolunden gectigi surece kabul edilir.
     */
    private static final Map<String, Set<String>> KNOWN_PROVIDERS = Map.of(
            "gmail", Set.of("gmail.com"),
            "hotmail", Set.of("hotmail.com"),
            "outlook", Set.of("outlook.com", "outlook.com.tr"),
            "yahoo", Set.of("yahoo.com", "yahoo.com.tr"),
            "icloud", Set.of("icloud.com"),
            "yandex", Set.of("yandex.com", "yandex.com.tr", "yandex.ru"),
            "proton", Set.of("proton.me", "protonmail.com"),
            "protonmail", Set.of("protonmail.com", "proton.me")
    );

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
    public RegisterResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());

        validateEmailProvider(email);

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

        return new RegisterResponse("Kayit olusturuldu. Giris yapabilirsiniz.", user.getUsername(), user.getRole());
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

    /** Bilinen saglayicilar icin alan adi dogrulamasi (is kurali, service katmaninda). */
    private void validateEmailProvider(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            throw new BadRequestException("Email adresi hatali");
        }
        String domain = email.substring(atIndex + 1).toLowerCase();
        String provider = domain.split("\\.")[0];

        Set<String> validDomains = KNOWN_PROVIDERS.get(provider);
        if (validDomains != null && !validDomains.contains(domain)) {
            String beklenen = String.join(" veya ",
                    validDomains.stream().map(d -> "@" + d).sorted().toList());
            throw new BadRequestException(
                    "Email adresi hatali: " + provider + " adresleri " + beklenen + " ile bitmelidir");
        }
    }
}
