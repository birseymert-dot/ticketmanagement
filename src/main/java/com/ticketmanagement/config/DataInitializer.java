package com.ticketmanagement.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ticketmanagement.model.entity.User;
import com.ticketmanagement.model.enums.Role;
import com.ticketmanagement.repository.UserRepository;

/**
 * Uygulama ayaga kalkarken varsayilan bir ADMIN kullanicisi olusturur.
 * (Register endpoint'i sadece USER rolunde kullanici olusturur;
 * sisteme ilk ADMIN'in bu sekilde eklenmesi gerekir.)
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       JdbcTemplate jdbcTemplate,
                                       PlatformTransactionManager transactionManager) {
        return args -> {
            fixAuditActionColumn(jdbcTemplate);
            new TransactionTemplate(transactionManager)
                    .executeWithoutResult(status -> compactUserIds(jdbcTemplate));
            deduplicateUsernames(userRepository);

            if (!userRepository.existsByUsernameIgnoreCase("admin")) {
                User admin = new User(
                        "admin",
                        "admin@ticketmanagement.com",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );
                admin.setId(nextFreeUserId(userRepository));
                userRepository.save(admin);
            }
        };
    }

    /** 1'den baslayarak kullanilmayan en kucuk kullanici ID'sini bulur. */
    private Long nextFreeUserId(UserRepository userRepository) {
        long expected = 1;
        for (Long id : userRepository.findAllIds()) {
            if (id == null || id < expected) continue;
            if (id != expected) break;
            expected++;
        }
        return expected;
    }

    /**
     * Kullanici ID'lerini 1'den baslayarak bosluksuz hale getirir.
     * Testler ve silinen kayitlar nedeniyle olusan atlamalari (orn. yeni
     * kullanicinin 65 numara almasi) duzeltir. Ticket ve yorumlardaki
     * kullanici baglantilari da ayni islemde guncellendigi icin veri
     * butunlugu korunur. Tek transaction icinde calisir.
     */
    private void compactUserIds(JdbcTemplate jdbcTemplate) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT ID FROM USERS ORDER BY ID", Long.class);

        List<long[]> moves = new ArrayList<>();
        long expected = 1;
        for (Long id : ids) {
            if (id != expected) {
                moves.add(new long[]{id, expected});
            }
            expected++;
        }
        if (moves.isEmpty()) {
            return;
        }

        // Ayni transaction'daki baglanti uzerinde FK kontrolu gecici kapatilir;
        // kullanici + ticket + yorum guncellemeleri birlikte yapilip geri acilir.
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        try {
            for (long[] move : moves) {
                long from = move[0];
                long to = move[1];
                jdbcTemplate.update("UPDATE USERS SET ID = ? WHERE ID = ?", to, from);
                jdbcTemplate.update("UPDATE TICKETS SET CREATED_BY_ID = ? WHERE CREATED_BY_ID = ?", to, from);
                jdbcTemplate.update("UPDATE TICKETS SET ASSIGNED_TO_ID = ? WHERE ASSIGNED_TO_ID = ?", to, from);
                jdbcTemplate.update("UPDATE COMMENTS SET AUTHOR_ID = ? WHERE AUTHOR_ID = ?", to, from);
            }
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    /**
     * Eski veritabanlarinda AUDIT_LOGS.ACTION kolonu, tablo olusturuldugu
     * andaki enum degerleriyle sinirli bir ENUM tipi olarak yaratilmis olabilir.
     * Enum'a sonradan eklenen degerler (orn. USER_DELETED) bu kolona yazilamaz
     * ve islem 500 hatasiyla patlar. Kolon VARCHAR'a cevrilerek bugunku ve
     * gelecekteki tum enum degerlerine uyumlu hale getirilir (idempotent).
     */
    private void fixAuditActionColumn(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE AUDIT_LOGS ALTER COLUMN ACTION VARCHAR(50) NOT NULL");
        } catch (Exception e) {
            // Tablo henuz yoksa veya veritabani desteklemiyorsa sessizce gecilir;
            // yeni olusan tablolarda sorun zaten olusmaz.
        }
        // Ayni sorun TICKETS.STATUS icin de gecerli: eski veritabanlarinda kolon
        // o gunki enum degerleriyle sinirli ENUM tipinde olusmus olabilir;
        // HOLD gibi sonradan eklenen degerler yazilamaz. VARCHAR'a cevrilir.
        try {
            jdbcTemplate.execute("ALTER TABLE TICKETS ALTER COLUMN STATUS VARCHAR(30) NOT NULL");
        } catch (Exception e) {
            // sessizce gecilir
        }
        // USERS.DEPARTMENT icin ayni onlem: ileride yeni departman eklenirse
        // eski veritabanlarinda ENUM kisiti hataya yol acmasin.
        try {
            jdbcTemplate.execute("ALTER TABLE USERS ALTER COLUMN DEPARTMENT VARCHAR(30)");
        } catch (Exception e) {
            // sessizce gecilir
        }
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
