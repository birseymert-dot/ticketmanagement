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

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Genel e-posta format kurali: kullanici@alanadi.uzanti
     * Alan adi en az bir nokta icermeli, uzanti en az 2 harf olmalidir.
     * Formata uymayan tum adresler "Email adresi hatali" ile reddedilir;
     * formata uyan her alan adi (gmail.com, firma.com.tr, sirket.io vb.) kabul edilir.
     */
    private static final Pattern EMAIL_FORMAT = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PROFILE_IMAGE_FORMAT = Pattern.compile(
            "^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=\\r\\n]+$",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_PROFILE_IMAGE_LENGTH = 300_000;

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
        String username = canonical(request.getUsername());
        String email = canonical(request.getEmail());

        validateEmailFormat(email);
        if (request.getDepartment() == null) {
            throw new BadRequestException("Departman secimi zorunludur");
        }

        // Benzersizlik kontrolleri buyuk/kucuk harf duyarsizdir:
        // "Cansu" kayitliyken "cansu" ile ikinci hesap acilamaz.
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Bu kullanici adi zaten kullaniliyor");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Bu email zaten kullaniliyor");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.USER,
                request.getDepartment()
        );
        user.setProfileImage(normalizeProfileImage(request.getProfileImage()));
        // En kucuk bos ID atanir; silinen kullanicilarin numaralari yeniden kullanilir
        user.setId(nextFreeUserId());
        userRepository.save(user);

        return new RegisterResponse("Kayit olusturuldu. Giris yapabilirsiniz.",
                user.getUsername(), user.getRole(), user.getDepartment(), user.getProfileImage());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadRequestException("Kullanici bulunamadi"));

        auditLogService.log(AuditAction.LOGIN, user.getUsername(), null, "Kullanici giris yapti");

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), user.getDepartment(), user.getProfileImage());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Kullanici adi ve email kucuk harfe sabitlenerek saklanir.
     * Boylece "Cansu" / "cansu" / "CANSU" ayni hesaba isaret eder ve
     * Turkce buyuk-I donusumu kaynakli karsilastirma hatalari onlenir.
     */
    private String canonical(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    /** 1'den baslayarak kullanilmayan en kucuk kullanici ID'sini bulur. */
    private Long nextFreeUserId() {
        long expected = 1;
        for (Long id : userRepository.findAllIds()) {
            if (id == null || id < expected) continue;
            if (id != expected) break;
            expected++;
        }
        return expected;
    }

    /** Genel e-posta format dogrulamasi (is kurali, service katmaninda). */
    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_FORMAT.matcher(email).matches()) {
            throw new BadRequestException("Email adresi hatali");
        }
    }

    private String normalizeProfileImage(String profileImage) {
        if (profileImage == null || profileImage.trim().isEmpty()) {
            return null;
        }

        String value = profileImage.trim();
        if (value.length() > MAX_PROFILE_IMAGE_LENGTH || !PROFILE_IMAGE_FORMAT.matcher(value).matches()) {
            throw new BadRequestException("Profil fotografi formati hatali");
        }
        return value;
    }
}
