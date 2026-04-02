package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Candidate;
import com.hrtech.resume_screening.entity.Resume;
import com.hrtech.resume_screening.repository.CandidateRepository;
import com.hrtech.resume_screening.repository.ResumeSkillRepository;
import com.hrtech.resume_screening.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
@Slf4j
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JwtUtil jwtUtil;

    public CandidateController(
            CandidateRepository candidateRepository,
            ResumeSkillRepository resumeSkillRepository,
            JwtUtil jwtUtil) {
        this.candidateRepository   = candidateRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.jwtUtil               = jwtUtil;
    }

    // ================================================================
    // GET ALL CANDIDATES — returns clean flat list (no nested resumes)
    // ================================================================
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCandidates(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        String email = extractEmail(auth);
        log.info("GET candidates for: {}", email);

        List<Candidate> candidates = email.equals("unknown")
                ? candidateRepository.findAll()
                : candidateRepository.findByCreatedByEmail(email);

        // Convert to clean flat maps — no nested objects
        List<Map<String, Object>> result = new ArrayList<>();
        for (Candidate c : candidates) {
            result.add(toCleanMap(c));
        }

        return ResponseEntity.ok(result);
    }

    // ================================================================
    // GET ONE CANDIDATE
    // ================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCandidateById(
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Candidate c = candidateRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));
            response.put("success", true);
            response.put("data", toCleanMap(c));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // UPDATE STATUS
    // ================================================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Candidate c = candidateRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));
            c.setStatus(body.get("status"));
            candidateRepository.save(c);
            response.put("success", true);
            response.put("data", toCleanMap(c));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // DELETE CANDIDATE
    // ================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCandidate(
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!candidateRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Candidate not found");
                return ResponseEntity.ok(response);
            }

            Candidate candidate = candidateRepository
                    .findById(id).orElse(null);

            if (candidate != null && candidate.getResumes() != null) {
                for (Resume resume : candidate.getResumes()) {
                    // Delete resume skills first (FK constraint)
                    List<com.hrtech.resume_screening.entity.ResumeSkill> skills =
                            resumeSkillRepository.findAll().stream()
                                    .filter(rs -> rs.getResume().getId()
                                            .equals(resume.getId()))
                                    .toList();
                    resumeSkillRepository.deleteAll(skills);
                    log.info("Deleted {} skills for resume {}",
                            skills.size(), resume.getId());
                }
            }

            candidateRepository.deleteById(id);
            log.info("Candidate {} permanently deleted", id);

            response.put("success", true);
            response.put("message", "Deleted successfully");

        } catch (Exception e) {
            log.error("Delete error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // HELPER — Convert Candidate to clean flat Map (no nesting)
    // ================================================================
    private Map<String, Object> toCleanMap(Candidate c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",               c.getId());
        map.put("fullName",         c.getFullName());
        map.put("email",            c.getEmail());
        map.put("phone",            c.getPhone());
        map.put("linkedinUrl",      c.getLinkedinUrl());
        map.put("githubUrl",        c.getGithubUrl());
        map.put("totalExperience",  c.getTotalExperience());
        map.put("totalScore",       c.getTotalScore());
        map.put("status",           c.getStatus());
        map.put("createdByEmail",   c.getCreatedByEmail());
        map.put("createdAt",        c.getCreatedAt());
        // Resume count only — no nested resume data
        int resumeCount = (c.getResumes() != null)
                ? c.getResumes().size() : 0;
        map.put("resumeCount", resumeCount);
        return map;
    }

    // ================================================================
    // HELPER — Extract email from JWT token
    // ================================================================
    private String extractEmail(String auth) {
        if (auth == null || auth.isBlank()) return "unknown";
        try {
            String token = auth.replace("Bearer ", "").trim();
            return jwtUtil.extractEmail(token);
        } catch (Exception e) {
            log.error("Token error: {}", e.getMessage());
            return "unknown";
        }
    }
}
