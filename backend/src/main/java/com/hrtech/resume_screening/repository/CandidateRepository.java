package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    Optional<Candidate> findFirstByEmail(String email);

    Optional<Candidate> findFirstByFullNameAndCreatedByEmail(
            String fullName, String createdByEmail);

    List<Candidate> findByCreatedByEmail(String email);

    Optional<Candidate> findByEmailAndCreatedByEmail(
            String email, String createdByEmail);
}