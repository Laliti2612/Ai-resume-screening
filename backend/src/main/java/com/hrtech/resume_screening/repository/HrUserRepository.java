package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.HrUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HrUserRepository
        extends JpaRepository<HrUser, Long> {

    Optional<HrUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM HrUser u WHERE " +
            "LOWER(u.email) = LOWER(:email)")
    Optional<HrUser> findByEmailIgnoreCase(
            @Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM HrUser u WHERE " +
            "LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(
            @Param("email") String email);
}