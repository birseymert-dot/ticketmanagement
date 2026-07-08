package com.ticketmanagement.config;

import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        // 1. adim: her kullanicinin hedef (kucuk harf + benzersiz) adini hesapla
        Map<Long, String> targetNames = new HashMap<>();
        Set<String> seenNames = new HashSet<>();
        for (User user : users) {
            String base = user.getUsername().trim().toLowerCase(Locale.ROOT);
            String target = seenNames.add(base) ? base : base + "_" + user.getId();
            targetNames.put(user.getId(), target);
        }

        // 2. adim: adi degisecek kayitlari once gecici benzersiz ada tasi.
        // Dogrudan yazilsaydi (orn. 'Cansu' -> 'cansu') hedef ad henuz baska
        // bir kayitta durdugu icin unique index ihlali olusurdu.
        for (User user : users) {
            if (!targetNames.get(user.getId()).equals(user.getUsername())) {
                user.setUsername("__gecici_" + user.getId());
                userRepository.save(user);
            }
        }

        // 3. adim: final adlari yaz, emaili kucuk harfe normalize et.
        // Email normalizasyonu baska bir kayitla cakisacaksa email oldugu gibi birakilir.
        Set<String> seenEmails = new HashSet<>();
        for (User user : users) {
            boolean changed = false;
            String targetName = targetNames.get(user.getId());
            if (!targetName.equals(user.getUsername())) {
                user.setUsername(targetName);
                changed = true;
            }
            String loweredEmail = user.getEmail().trim().toLowerCase(Locale.ROOT);
            if (seenEmails.add(loweredEmail) && !loweredEmail.equals(user.getEmail())) {
                user.setEmail(loweredEmail);
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }
    }
}
