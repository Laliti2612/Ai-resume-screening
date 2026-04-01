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

            // Skip empty or wrong length
            if (line.isEmpty() || line.length() < 3
                    || line.length() > 60) continue;

            // Skip lines with special characters
            if (line.contains("@"))          continue;
            if (line.matches(".*\\d{5,}.*")) continue;
            if (line.contains("http"))       continue;
            if (line.contains("www."))       continue;
            if (line.contains("|"))          continue;
            if (line.contains("/"))          continue;
            if (line.contains(":"))          continue;
            if (line.contains(","))          continue;
            if (line.contains("("))          continue;

            // Skip resume section headers
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

            // Must look like a proper name (letters and spaces only)
            if (line.matches("[A-Za-z]+(\\s[A-Za-z]+){0,3}")) {
                log.info("Name extracted: {}", line);
                return line;
            }
        }

        log.warn("Name not found in resume — using Unknown Candidate");
        return "Unknown Candidate";
    }
}