package com.ticketmanagement.config;

import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Uygulama ayaga kalkarken varsayilan bir ADMIN kullanicisi olusturur.
 * (Register endpoint'i sadece USER rolunde kullanici olusturur;
 * sisteme ilk ADMIN'in bu sekilde eklenmesi gerekir.)
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User(
                        "admin",
                        "admin@ticketmanagement.com",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );
                userRepository.save(admin);
            }
        };
    }
}
