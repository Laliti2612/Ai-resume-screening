package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.CandidateScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends JpaRepository<CandidateScore, Long> {

}