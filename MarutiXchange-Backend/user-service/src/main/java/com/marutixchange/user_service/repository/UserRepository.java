package com.marutixchange.user_service.repository;

import com.marutixchange.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    // ================= BASIC =================

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ================= PASSWORD RESET =================

    Optional<User> findByResetToken(String token);

    // ================= SECURITY =================

    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmailAndActiveTrue(String email);
}