package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Job;
import com.hrtech.resume_screening.security.JwtUtil;
import com.hrtech.resume_screening.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
@Slf4j
public class JobController {

    private final JobService jobService;
    private final JwtUtil    jwtUtil;

    public JobController(JobService jobService,
                         JwtUtil jwtUtil) {
        this.jobService = jobService;
        this.jwtUtil    = jwtUtil;
    }

    // ── Get All Jobs (filtered by user) ───────────────────────
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        String email = extractEmail(auth);
        log.info("GET JOBS | user={}", email);
        return ResponseEntity.ok(
                jobService.getJobsByEmail(email));
    }

    // ── Create Job ────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createJob(
            @RequestBody Job job,
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {
        try {
            String email = extractEmail(auth);
            if (job.getStatus() == null)
                job.setStatus("ACTIVE");
            job.setCreatedByEmail(email);

            log.info("CREATE JOB | {} | user={}",
                    job.getTitle(), email);

            Job saved = jobService.saveJob(job);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Create job failed: {}",
                    e.getMessage());
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }

    // ── Update Job ────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(
            @PathVariable Long id,
            @RequestBody Job job,
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {
        try {
            String email = extractEmail(auth);
            job.setId(id);
            job.setCreatedByEmail(email);

            log.info("UPDATE JOB id={} | user={}",
                    id, email);

            return ResponseEntity.ok(
                    jobService.saveJob(job));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }

    // ── Delete Job ────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long id) {
        log.info("DELETE JOB id={}", id);
        jobService.deleteJob(id);
        return ResponseEntity.ok("Deleted");
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