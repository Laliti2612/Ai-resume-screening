package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.Candidate;
import com.hrtech.resume_screening.repository
        .CandidateRepository;
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
    private final JwtUtil             jwtUtil;

    public CandidateController(
            CandidateRepository candidateRepository,
            JwtUtil jwtUtil) {
        this.candidateRepository = candidateRepository;
        this.jwtUtil             = jwtUtil;
    }

    // ── Get All Candidates (filtered by user) ─────────────────
    @GetMapping
    public ResponseEntity<Map<String, Object>>
    getAllCandidates(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        String email = extractEmail(auth);
        log.info("GET CANDIDATES | user={}", email);

        List<Candidate> candidates;
        if (email.equals("unknown")) {
            candidates = candidateRepository.findAll();
        } else {
            candidates = candidateRepository
                    .findByCreatedByEmail(email);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data",    candidates);
        return ResponseEntity.ok(response);
    }

    // ── Get Candidate By ID ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>>
    getCandidateById(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            Candidate candidate = candidateRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Not found"));
            response.put("success", true);
            response.put("data",    candidate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ── Update Status ─────────────────────────────────────────
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>>
    updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        try {
            Candidate candidate = candidateRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Not found"));
            candidate.setStatus(body.get("status"));
            candidateRepository.save(candidate);

            log.info("Status updated | id={} status={}",
                    id, body.get("status"));

            response.put("success", true);
            response.put("data",    candidate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ── Delete Candidate ──────────────────────────────────────
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
            candidateRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Deleted successfully");
        } catch (Exception e) {
            log.error("Delete error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
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