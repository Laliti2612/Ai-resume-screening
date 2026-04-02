package com.hrtech.resume_screening.service;

import com.hrtech.resume_screening.entity.Candidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CandidateExtractorService {

    public Candidate extractCandidate(String text) {
        Candidate candidate = new Candidate();
        candidate.setEmail(extractEmail(text));
        candidate.setPhone(extractPhone(text));
        candidate.setFullName(extractName(text));
        log.info("Extracted => Name: {} | Email: {} | Phone: {}",
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getPhone());
        return candidate;
    }

    // ================================================================
    // NEW — Determine Status from Score (5 bands)
    // ================================================================
    /**
     * Score Bands:
     * 85–100 → AUTO_SHORTLISTED  (Excellent)
     * 70–84  → SHORTLISTED       (Good)
     * 55–69  → UNDER_CONSIDERATION (Average)
     * 40–54  → REJECTED          (Below Average)
     * 0–39   → AUTO_REJECTED     (Poor)
     */
    public String determineStatus(double score) {
        if (score >= 85) return "AUTO_SHORTLISTED";
        if (score >= 70) return "SHORTLISTED";
        if (score >= 55) return "UNDER_CONSIDERATION";
        if (score >= 40) return "REJECTED";
        return "AUTO_REJECTED";
    }

    // ================================================================
    // EXISTING — Extract candidate info from resume text (unchanged)
    // ================================================================
    private String extractEmail(String text) {
        Pattern p = Pattern.compile(
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group();
        return "unknown@email.com";
    }

    private String extractPhone(String text) {
        Pattern p = Pattern.compile(
                "(\\+91[\\s-]?)?[6-9][0-9]{9}");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group().replaceAll("[\\s-]", "");
        return "0000000000";
    }

    private String extractName(String text) {
        String[] lines = text.split("\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.length() < 3
                    || line.length() > 60) continue;

            if (line.contains("@"))          continue;
            if (line.matches(".*\\d{5,}.*")) continue;
            if (line.contains("http"))       continue;
            if (line.contains("www."))       continue;
            if (line.contains("|"))          continue;
            if (line.contains("/"))          continue;
            if (line.contains(":"))          continue;
            if (line.contains(","))          continue;
            if (line.contains("("))          continue;

            String lower = line.toLowerCase();
            if (lower.contains("resume")      ||
                    lower.contains("curriculum")  ||
                    lower.contains("objective")   ||
                    lower.contains("summary")     ||
                    lower.contains("experience")  ||
                    lower.contains("education")   ||
                    lower.contains("skill")       ||
                    lower.contains("project")     ||
                    lower.contains("address")     ||
                    lower.contains("contact")     ||
                    lower.contains("phone")       ||
                    lower.contains("mobile")      ||
                    lower.contains("email")       ||
                    lower.contains("linkedin")    ||
                    lower.contains("github")      ||
                    lower.contains("declaration") ||
                    lower.contains("reference")   ||
                    lower.contains("language")    ||
                    lower.contains("hobby")       ||
                    lower.contains("interest"))   continue;

            if (line.matches("[A-Za-z]+(\\s[A-Za-z]+){0,3}")) {
                log.info("Name extracted: {}", line);
                return line;
            }
        }

        log.warn("Name not found in resume — using Unknown Candidate");
        return "Unknown Candidate";
    }
}
