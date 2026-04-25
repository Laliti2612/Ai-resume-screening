package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Job;
import com.hrtech.resume_screening.entity.Resume;
import com.hrtech.resume_screening.repository.JobRepository;
import com.hrtech.resume_screening.repository.ResumeRepository;
import com.hrtech.resume_screening.security.JwtUtil;
import com.hrtech.resume_screening.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
@Slf4j
public class ResumeController {

    private final ResumeService    resumeService;
    private final ResumeRepository resumeRepository;
    private final JobRepository    jobRepository;
    private final JwtUtil          jwtUtil;

    public ResumeController(
            ResumeService resumeService,
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            JwtUtil jwtUtil) {
        this.resumeService    = resumeService;
        this.resumeRepository = resumeRepository;
        this.jobRepository    = jobRepository;
        this.jwtUtil          = jwtUtil;
    }

    // ── Upload Resume (authenticated HR) ─────────────────────
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobId",
                    required = false) Long jobId,
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = extractEmail(auth);

            log.info("UPLOAD | file={} jobId={} user={}",
                    file.getOriginalFilename(),
                    jobId, userEmail);

            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result =
                    resumeService.uploadResume(
                            file, jobId, userEmail);

            response.put("success", true);
            response.put("data",    result);
            response.put("message", "Resume uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Public Upload (no auth — candidate applies via share link) ──
    @PostMapping("/upload/public")
    public ResponseEntity<Map<String, Object>> uploadPublic(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobId") Long jobId) {

        Map<String, Object> response = new HashMap<>();
        try {
            log.info("PUBLIC UPLOAD | file={} jobId={}",
                    file.getOriginalFilename(), jobId);

            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getOriginalFilename()
                    .toLowerCase().endsWith(".pdf")) {
                response.put("success", false);
                response.put("message", "Only PDF files accepted");
                return ResponseEntity.badRequest().body(response);
            }

            // ── Get job and HR email ──────────────────────────
            // Candidate will appear in the HR's dashboard
            // who created this job
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                response.put("success", false);
                response.put("message", "Job not found");
                return ResponseEntity.status(404).body(response);
            }

            // Use the HR's email who owns this job
            // So candidate shows up in their candidates list
            String hrEmail = job.getCreatedByEmail();
            if (hrEmail == null || hrEmail.isBlank()) {
                hrEmail = "public_applicant@shared.link";
            }

            log.info("PUBLIC UPLOAD | Job: {} | Assigning to HR: {}",
                    job.getTitle(), hrEmail);

            // Process resume — same AI scoring as normal upload
            Map<String, Object> result =
                    resumeService.uploadResume(file, jobId, hrEmail);

            response.put("success", true);
            response.put("data",    result);
            response.put("message", "Application submitted successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Public upload failed: {}", e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Get All Resumes ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllResumes(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = extractEmail(auth);
            List<Resume> resumes;

            if (userEmail.equals("unknown")) {
                resumes = resumeRepository.findAll();
            } else {
                resumes = resumeRepository
                        .findByCandidateCreatedByEmail(userEmail);
            }

            response.put("success", true);
            response.put("data",    resumes);
            response.put("count",   resumes.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Get Resume By ID ──────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getResumeById(
            @PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            Resume resume = resumeRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Resume not found: " + id));
            response.put("success", true);
            response.put("data",    resume);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    // ── Delete Resume ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResume(
            @PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            resumeRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Resume deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Extract email from JWT token ──────────────────────────
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
