package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeSkillRepository
        extends JpaRepository<ResumeSkill, Long> {

    boolean existsByResumeIdAndSkillId(Long resumeId, Long skillId);
}