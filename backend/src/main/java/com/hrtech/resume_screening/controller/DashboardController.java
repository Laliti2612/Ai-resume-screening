package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Candidate;
import com.hrtech.resume_screening.entity.Resume;
import com.hrtech.resume_screening.repository
        .CandidateRepository;
import com.hrtech.resume_screening.repository
        .JobRepository;
import com.hrtech.resume_screening.repository
        .ResumeRepository;
import com.hrtech.resume_screening.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Slf4j
public class DashboardController {

    private final CandidateRepository candidateRepository;
    private final JobRepository       jobRepository;
    private final ResumeRepository    resumeRepository;
    private final JwtUtil             jwtUtil;

    public DashboardController(
            CandidateRepository candidateRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            JwtUtil jwtUtil) {
        this.candidateRepository = candidateRepository;
        this.jobRepository       = jobRepository;
        this.resumeRepository    = resumeRepository;
        this.jwtUtil             = jwtUtil;
    }

    // ── Dashboard Stats ───────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        String email = extractEmail(auth);
        log.info("DASHBOARD STATS | user={}", email);

        List<Candidate> all;
        List<Resume>    resumes;
        long            totalJobs;

        if (email.equals("unknown")) {
            all       = candidateRepository.findAll();
            resumes   = resumeRepository.findAll();
            totalJobs = jobRepository.count();
        } else {
            all       = candidateRepository
                    .findByCreatedByEmail(email);
            resumes   = resumeRepository
                    .findByCandidateCreatedByEmail(email);
            totalJobs = jobRepository
                    .findByCreatedByEmail(email).size();
        }

        // Candidate stats
        long total       = all.size();
        long shortlisted = all.stream().filter(
                c -> "SHORTLISTED"
                        .equals(c.getStatus())).count();
        long rejected    = all.stream().filter(
                c -> "REJECTED"
                        .equals(c.getStatus())).count();
        long newCount    = all.stream().filter(
                c -> "NEW"
                        .equals(c.getStatus())).count();
        long hired       = all.stream().filter(
                c -> "HIRED"
                        .equals(c.getStatus())).count();

        // Score distribution
        long excellent = all.stream().filter(
                c -> c.getTotalScore() != null
                        && c.getTotalScore() >= 85).count();
        long good      = all.stream().filter(
                c -> c.getTotalScore() != null
                        && c.getTotalScore() >= 70
                        && c.getTotalScore() < 85).count();
        long average   = all.stream().filter(
                c -> c.getTotalScore() != null
                        && c.getTotalScore() >= 45
                        && c.getTotalScore() < 70).count();
        long poor      = all.stream().filter(
                c -> c.getTotalScore() != null
                        && c.getTotalScore() < 45).count();

        Map<String, Object> data = new HashMap<>();
        data.put("total",        total);
        data.put("shortlisted",  shortlisted);
        data.put("rejected",     rejected);
        data.put("newCount",     newCount);
        data.put("hired",        hired);
        data.put("excellent",    excellent);
        data.put("good",         good);
        data.put("average",      average);
        data.put("poor",         poor);
        data.put("totalJobs",    totalJobs);
        data.put("totalResumes", resumes.size());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data",    data);
        return response;
    }

    // ── Extract email from JWT ────────────────────────────────
    private String extractEmail(String auth) {
        if (auth == null || auth.isBlank())
            return "unknown";
        try {
            String token = auth
                    .replace("Bearer ", "").trim();
            if (token.startsWith("eyJ")) {
                return jwtUtil.extractEmail(token);
            }
            return token
                    .replace("demo-token-", "").trim();
        } catch (Exception e) {
            log.warn("Token extraction failed: {}",
                    e.getMessage());
            return "unknown";
        }
    }
}