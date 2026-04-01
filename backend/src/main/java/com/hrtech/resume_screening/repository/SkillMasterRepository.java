package com.hrtech.resume_screening.repository;

import com.hrtech.resume_screening.entity.SkillMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SkillMasterRepository
        extends JpaRepository<SkillMaster, Long> {
    Optional<SkillMaster> findBySkillName(String skillName);
}