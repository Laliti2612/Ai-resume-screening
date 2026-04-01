package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity
        .PasswordResetOtp;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository
        .Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation
        .Transactional;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp>
    findTopByEmailOrderByCreatedAtDesc(
            String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOtp o " +
            "WHERE o.email = :email")
    void deleteByEmail(@Param("email") String email);
}