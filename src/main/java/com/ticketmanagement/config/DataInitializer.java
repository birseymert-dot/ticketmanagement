package com.ticketmanagement.config;

import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
            deduplicateUsernames(userRepository);

            if (!userRepository.existsByUsernameIgnoreCase("admin")) {
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

    /**
     * Benzersizlik kurali harf duyarsiz hale getirilmeden once kaydedilmis
     * eski kayitlari duzeltir:
     * - Tum kullanici adlari ve emailler kucuk harfe normalize edilir.
     * - Mukerrer kullanici adlarinda (orn. "cansu" ve "Cansu") ilk acilan
     *   hesap adini korur, sonrakiler "ad_id" olarak yeniden adlandirilir.
     * Iliskiler kullanici ID'si uzerinden kuruldugu icin veri kaybi olmaz.
     */
    private void deduplicateUsernames(UserRepository userRepository) {
        List<User> users = userRepository.findAll(Sort.by("id"));
        Set<String> seen = new HashSet<>();
        for (User user : users) {
            boolean changed = false;
            String lowered = user.getUsername().trim().toLowerCase(Locale.ROOT);
            String loweredEmail = user.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!lowered.equals(user.getUsername())) { user.setUsername(lowered); changed = true; }
            if (!loweredEmail.equals(user.getEmail())) { user.setEmail(loweredEmail); changed = true; }
            if (!seen.add(lowered)) {
                user.setUsername(lowered + "_" + user.getId());
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }
    }
}
