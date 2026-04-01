package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByCandidateCreatedByEmail(String email);
}