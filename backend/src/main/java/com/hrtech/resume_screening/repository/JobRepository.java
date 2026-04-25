package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByCreatedByEmail(String email);

    Optional<Job> findByPublicId(String publicId);
}
