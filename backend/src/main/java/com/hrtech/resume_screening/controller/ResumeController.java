package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Resume;
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
    private final JwtUtil          jwtUtil;

    public ResumeController(
            ResumeService resumeService,
            ResumeRepository resumeRepository,
            JwtUtil jwtUtil) {
        this.resumeService    = resumeService;
        this.resumeRepository = resumeRepository;
        this.jwtUtil          = jwtUtil;
    }

    // ── Upload Resume ─────────────────────────────────────────
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
                return ResponseEntity
                        .badRequest().body(response);
            }

            Map<String, Object> result =
                    resumeService.uploadResume(
                            file, jobId, userEmail);

            response.put("success", true);
            response.put("data",    result);
            response.put("message",
                    "Resume uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed: {}",
                    e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message",
                    "Upload failed: " + e.getMessage());
            return ResponseEntity
                    .status(500).body(response);
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
                        .findByCandidateCreatedByEmail(
                                userEmail);
            }

            response.put("success", true);
            response.put("data",    resumes);
            response.put("count",   resumes.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity
                    .status(500).body(response);
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
            return ResponseEntity
                    .status(404).body(response);
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
            response.put("message",
                    "Resume deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message",
                    "Delete failed: " + e.getMessage());
            return ResponseEntity
                    .status(500).body(response);
        }
    }

    // ── Extract email from JWT token ──────────────────────────
    private String extractEmail(String auth) {
        if (auth == null || auth.isBlank())
            return "unknown";
        try {
            String token = auth
                    .replace("Bearer ", "").trim();
            // Real JWT token starts with eyJ
            if (token.startsWith("eyJ")) {
                return jwtUtil.extractEmail(token);
            }
            // Old demo token fallback
            return token
                    .replace("demo-token-", "").trim();
        } catch (Exception e) {
            log.warn("Token extraction failed: {}",
                    e.getMessage());
            return "unknown";
        }
    }
}