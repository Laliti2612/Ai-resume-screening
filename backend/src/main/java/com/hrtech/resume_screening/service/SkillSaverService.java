package com.hrtech.resume_screening.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrtech.resume_screening.entity.ResumeSkill;
import com.hrtech.resume_screening.entity.Resume;
import com.hrtech.resume_screening.entity.SkillMaster;
import com.hrtech.resume_screening.repository.ResumeSkillRepository;
import com.hrtech.resume_screening.repository.SkillMasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SkillSaverService {

    private final SkillMasterRepository skillMasterRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SkillSaverService(
            SkillMasterRepository skillMasterRepository,
            ResumeSkillRepository resumeSkillRepository) {
        this.skillMasterRepository = skillMasterRepository;
        this.resumeSkillRepository = resumeSkillRepository;
    }

    @Transactional
    public void saveSkills(String aiJsonResult, Resume resume) {
        try {
            String cleanJson = aiJsonResult
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode root = objectMapper.readTree(cleanJson);

            saveSkillArray(root.get("technical_skills"), "TECHNICAL", resume);
            saveSkillArray(root.get("soft_skills"), "SOFT", resume);
            saveSkillArray(root.get("tools"), "TOOL", resume);

            log.info("All skills saved for resume id: {}", resume.getId());

        } catch (Exception e) {
            log.error("Failed to save skills: {}", e.getMessage());
        }
    }

    @Transactional
    private void saveSkillArray(JsonNode skillArray,
                                String category, Resume resume) {
        if (skillArray == null || !skillArray.isArray()) return;

        for (JsonNode skillNode : skillArray) {
            try {
                String skillName = skillNode.asText().trim();
                if (skillName.isEmpty()) continue;

                // Find or create skill in skill_master
                SkillMaster skill = skillMasterRepository
                        .findBySkillName(skillName)
                        .orElseGet(() -> {
                            SkillMaster newSkill = new SkillMaster();
                            newSkill.setSkillName(skillName);
                            newSkill.setCategory(category);
                            return skillMasterRepository.saveAndFlush(newSkill);
                        });

                // Check duplicate before saving
                boolean exists = resumeSkillRepository
                        .existsByResumeIdAndSkillId(
                                resume.getId(), skill.getId());

                if (!exists) {
                    ResumeSkill resumeSkill = new ResumeSkill();
                    resumeSkill.setResume(resume);
                    resumeSkill.setSkill(skill);
                    resumeSkill.setConfidence(1.00);
                    resumeSkillRepository.saveAndFlush(resumeSkill);
                    log.info("Saved skill: {}", skillName);
                } else {
                    log.info("Skill already exists: {}", skillName);
                }

            } catch (Exception e) {
                log.error("Error saving skill: {}", e.getMessage());
            }
        }
    }
}