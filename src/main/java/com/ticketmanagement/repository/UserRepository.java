package com.ticketmanagement.repository;

import com.ticketmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /** Buyuk/kucuk harf duyarsiz arama: "Cansu" ile "cansu" ayni kullanicidir. */
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    /** Benzersizlik kontrolu buyuk/kucuk harf duyarsiz yapilir. */
    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Kullanilan tum ID'ler kucukten buyuge — bos ID bulmak icin. */
    @Query("SELECT u.id FROM User u ORDER BY u.id")
    List<Long> findAllIds();
}
