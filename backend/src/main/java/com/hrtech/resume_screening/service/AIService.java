package com.hrtech.resume_screening.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class AIService {

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    // ================================================================
    // EXISTING — Extract Skills via Groq AI (unchanged)
    // ================================================================
    public String extractSkills(String resumeText) {
        log.info("=== Calling Groq AI === Model: {}", model);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.0);
        requestBody.put("messages", List.of(
                Map.of("role", "system",
                        "content", buildSystemPrompt()),
                Map.of("role", "user",
                        "content", buildUserPrompt(resumeText))
        ));

        log.info("Sending request to Groq...");

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    GROQ_URL, request, Map.class);

            log.info("Groq status: {}", response.getStatusCode());

            Map<String, Object> body =
                    (Map<String, Object>) response.getBody();

            if (body == null) {
                log.error("Groq returned NULL body");
                return getDefaultResponse();
            }

            if (body.containsKey("error")) {
                log.error("Groq API error: {}", body.get("error"));
                return getDefaultResponse();
            }

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) body.get("choices");

            if (choices == null || choices.isEmpty()) {
                log.error("Groq empty choices. Body: {}", body);
                return getDefaultResponse();
            }

            Map<String, Object> choice  = choices.get(0);
            Map<String, Object> message =
                    (Map<String, Object>) choice.get("message");

            if (message == null) {
                log.error("Groq message null. Choice: {}", choice);
                return getDefaultResponse();
            }

            String content = (String) message.get("content");
            log.info("Raw Groq content: [{}]", content);

            if (content == null || content.isBlank()) {
                log.error("Groq content is empty");
                return getDefaultResponse();
            }

            // Clean markdown fences
            content = content
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            // Extract JSON object only
            int start = content.indexOf('{');
            int end   = content.lastIndexOf('}');

            if (start < 0 || end <= start) {
                log.error("No JSON found in: [{}]", content);
                return getDefaultResponse();
            }

            String jsonOnly = content.substring(start, end + 1);
            log.info("=== Final AI JSON ===\n{}", jsonOnly);
            return jsonOnly;

        } catch (HttpClientErrorException e) {
            log.error("Groq HTTP Error: {} | {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return getDefaultResponse();
        } catch (Exception e) {
            log.error("Groq Exception: {} | {}",
                    e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            return getDefaultResponse();
        }
    }

    public String testConnection() {
        log.info("=== Testing Groq Connection ===");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 100);
        requestBody.put("messages", List.of(
                Map.of("role", "user",
                        "content",
                        "Reply with only: {\"status\": \"ok\"}")
        ));

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    GROQ_URL, request, Map.class);
            return "SUCCESS: " + response.getStatusCode()
                    + " | " + response.getBody();
        } catch (HttpClientErrorException e) {
            return "FAILED: " + e.getStatusCode()
                    + " | " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "EXCEPTION: " + e.getMessage();
        }
    }

    // ================================================================
    // NEW — COMPOSITE SCORING ENGINE
    // ================================================================

    /**
     * Main entry point — computes total score (0–100)
     * Weights: Skill 45% | Experience 25% | Education 15% | Keyword 15%
     */
    public double computeTotalScore(List<String> resumeSkills,
                                    String resumeText,
                                    List<String> requiredSkills,
                                    int experienceMin,
                                    int experienceMax,
                                    int resumeYears,
                                    String jobDescription) {

        double skillScore = calculateSkillScore(resumeSkills, requiredSkills);
        double expScore   = calculateExperienceScore(resumeYears, experienceMin, experienceMax);
        double eduScore   = calculateEducationScore(resumeText);
        double kwScore    = calculateKeywordScore(resumeText, jobDescription);

        double total = (skillScore * 0.45)
                + (expScore   * 0.25)
                + (eduScore   * 0.15)
                + (kwScore    * 0.15);

        log.info("=== SCORING BREAKDOWN ===");
        log.info("  Skill Match  (45%): {}", String.format("%.2f", skillScore));
        log.info("  Experience   (25%): {}", String.format("%.2f", expScore));
        log.info("  Education    (15%): {}", String.format("%.2f", eduScore));
        log.info("  Keyword Match(15%): {}", String.format("%.2f", kwScore));
        log.info("  TOTAL SCORE       : {}", String.format("%.2f", total));

        return Math.round(total * 100.0) / 100.0;
    }

    // ----------------------------------------------------------------
    // DIMENSION 1: Skill Match Score (45%)
    // Matched skills ÷ required skills × 100
    // Partial credit for aliases
    // ----------------------------------------------------------------
    private double calculateSkillScore(List<String> resumeSkills,
                                       List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            log.warn("No required skills defined for job — skill score defaulted to 50");
            return 50.0;
        }
        if (resumeSkills == null || resumeSkills.isEmpty()) {
            log.warn("Resume has no skills — skill score = 0");
            return 0.0;
        }

        long matched = requiredSkills.stream()
                .filter(req -> resumeSkills.stream()
                        .anyMatch(res -> isSkillMatch(res, req)))
                .count();

        double score = (double) matched / requiredSkills.size() * 100.0;
        log.info("Skill Match: {}/{} required skills matched → {}",
                matched, requiredSkills.size(), String.format("%.2f", score));
        return score;
    }

    /**
     * Skill matching with alias support and partial matching
     */
    private boolean isSkillMatch(String resumeSkill, String requiredSkill) {
        if (resumeSkill == null || requiredSkill == null) return false;

        String r   = resumeSkill.toLowerCase().trim();
        String req = requiredSkill.toLowerCase().trim();

        // Exact match
        if (r.equals(req)) return true;

        // Partial / contains match
        if (r.contains(req) || req.contains(r)) return true;

        // Common tech aliases
        Map<String, String> aliases = new HashMap<>();
        aliases.put("js",           "javascript");
        aliases.put("ts",           "typescript");
        aliases.put("ml",           "machine learning");
        aliases.put("ai",           "artificial intelligence");
        aliases.put("k8s",          "kubernetes");
        aliases.put("py",           "python");
        aliases.put("react.js",     "react");
        aliases.put("reactjs",      "react");
        aliases.put("node.js",      "nodejs");
        aliases.put("nodej",        "nodejs");
        aliases.put("vue.js",       "vue");
        aliases.put("vuejs",        "vue");
        aliases.put("next.js",      "nextjs");
        aliases.put("postgres",     "postgresql");
        aliases.put("mongo",        "mongodb");
        aliases.put("springboot",   "spring boot");
        aliases.put("spring-boot",  "spring boot");
        aliases.put("c#",           "csharp");
        aliases.put("dotnet",       ".net");
        aliases.put("ms sql",       "mssql");
        aliases.put("mysql server", "mysql");

        String rNorm   = aliases.getOrDefault(r, r);
        String reqNorm = aliases.getOrDefault(req, req);
        return rNorm.equals(reqNorm);
    }

    // ----------------------------------------------------------------
    // DIMENSION 2: Experience Score (25%)
    // In range = 100 | Below min = proportional up to 30
    // Above max = 90 (slight penalty)
    // ----------------------------------------------------------------
    private double calculateExperienceScore(int resumeYears,
                                            int minExp,
                                            int maxExp) {
        // Fresher job (0-0 range) and fresher candidate
        if (minExp == 0 && maxExp == 0 && resumeYears == 0) return 100.0;

        // Within range
        if (resumeYears >= minExp && resumeYears <= maxExp) return 100.0;

        // Above max — still usable, minor penalty
        if (resumeYears > maxExp) return 90.0;

        // Below min — proportional interpolation
        if (minExp == 0) return 100.0; // no minimum required
        double ratio = (double) resumeYears / minExp;
        return Math.max(0.0, ratio * 30.0);
    }

    // ----------------------------------------------------------------
    // DIMENSION 3: Education Score (15%)
    // PhD=100 | Master=90 | Bachelor=75 | Diploma=50 | Other=30
    // ----------------------------------------------------------------
    private double calculateEducationScore(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) return 30.0;

        String text = resumeText.toLowerCase();

        if (text.contains("ph.d")     ||
                text.contains("phd")      ||
                text.contains("doctorate") ||
                text.contains("doctor of philosophy")) return 100.0;

        if (text.contains("master")   ||
                text.contains("m.tech")   ||
                text.contains("m.e.")     ||
                text.contains("m.sc")     ||
                text.contains("msc")      ||
                text.contains("mba")      ||
                text.contains("m.b.a")    ||
                text.contains("m.ca")     ||
                text.contains("mca")      ||
                text.contains("post graduate") ||
                text.contains("postgraduate")) return 90.0;

        if (text.contains("bachelor") ||
                text.contains("b.tech")   ||
                text.contains("b.e")      ||
                text.contains("b.sc")     ||
                text.contains("bsc")      ||
                text.contains("b.com")    ||
                text.contains("bca")      ||
                text.contains("b.ca")     ||
                text.contains("b.a")      ||
                text.contains("under graduate") ||
                text.contains("undergraduate")) return 75.0;

        if (text.contains("diploma")  ||
                text.contains("polytechnic") ||
                text.contains("12th")     ||
                text.contains("hsc")      ||
                text.contains("higher secondary")) return 50.0;

        return 30.0; // SSC / 10th / unknown
    }

    // ----------------------------------------------------------------
    // DIMENSION 4: Keyword Match Score (15%) — TF-IDF style
    // Unique JD keywords found in resume ÷ total unique JD keywords
    // ----------------------------------------------------------------
    private double calculateKeywordScore(String resumeText,
                                         String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) {
            log.warn("Job description empty — keyword score defaulted to 50");
            return 50.0;
        }
        if (resumeText == null || resumeText.isBlank()) return 0.0;

        // Common stop words to skip
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "and", "for", "with", "that", "this",
                "are", "will", "have", "from", "they", "you",
                "your", "our", "all", "any", "can", "been",
                "has", "not", "but", "its", "than", "then",
                "when", "also", "into", "more", "some", "such",
                "work", "team", "good", "able", "must", "well"
        ));

        String[] jdWords = jobDescription.toLowerCase().split("\\W+");
        String resumeLower = resumeText.toLowerCase();

        long matched = Arrays.stream(jdWords)
                .filter(w -> w.length() > 3)
                .filter(w -> !stopWords.contains(w))
                .distinct()
                .filter(resumeLower::contains)
                .count();

        long total = Arrays.stream(jdWords)
                .filter(w -> w.length() > 3)
                .filter(w -> !stopWords.contains(w))
                .distinct()
                .count();

        if (total == 0) return 50.0;

        double score = (double) matched / total * 100.0;
        log.info("Keyword Match: {}/{} unique keywords → {}",
                matched, total, String.format("%.2f", score));
        return score;
    }

    // ================================================================
    // NEW — Determine Status from Score (5 bands)
    // ================================================================
    public String determineStatus(double score) {
        if (score >= 85) return "AUTO_SHORTLISTED";    // Excellent
        if (score >= 70) return "SHORTLISTED";          // Good
        if (score >= 55) return "UNDER_CONSIDERATION";  // Average
        if (score >= 40) return "REJECTED";             // Below Average
        return "AUTO_REJECTED";                         // Poor
    }

    // ================================================================
    // PRIVATE HELPERS — Prompts & Default Response (unchanged)
    // ================================================================
    private String buildSystemPrompt() {
        return "You are a resume parser. " +
                "You MUST respond with ONLY a valid JSON object. " +
                "No explanation. No markdown. No extra text. " +
                "Start directly with { and end with }. " +
                "NEVER add skills that are not explicitly " +
                "written in the resume. " +
                "If resume has no technical skills, " +
                "return empty arrays.";
    }

    private String buildUserPrompt(String resumeText) {
        String truncated = resumeText.substring(
                0, Math.min(resumeText.length(), 3000));

        return "Parse ONLY the following resume text.\n" +
                "Do NOT assume or invent any skills.\n" +
                "Only extract what is EXPLICITLY written.\n\n" +
                "Return ONLY this JSON:\n" +
                "{\n" +
                "  \"technical_skills\": [],\n" +
                "  \"soft_skills\": [],\n" +
                "  \"tools\": [],\n" +
                "  \"certifications\": [],\n" +
                "  \"education\": {\n" +
                "    \"degree\": \"B.Tech\",\n" +
                "    \"field\": \"Computer Science\",\n" +
                "    \"institution\": \"University\",\n" +
                "    \"year\": 2024\n" +
                "  },\n" +
                "  \"experience_years\": 0,\n" +
                "  \"is_fresher\": true,\n" +
                "  \"summary\": \"two sentence summary\"\n" +
                "}\n\n" +
                "STRICT RULES:\n" +
                "- ONLY extract skills EXPLICITLY written " +
                "in the resume\n" +
                "- Do NOT guess, assume or hallucinate skills\n" +
                "- If resume has NO technical skills → " +
                "technical_skills: []\n" +
                "- If resume has NO tools → tools: []\n" +
                "- BPO/Non-IT resume → " +
                "technical_skills: [], tools: []\n" +
                "- experience_years = ONLY paid full-time jobs\n" +
                "- Internships/projects = 0 experience\n" +
                "- is_fresher = true if no full-time job\n" +
                "- Output ONLY the JSON object, nothing else\n\n" +
                "RESUME TEXT TO PARSE:\n" + truncated;
    }

    private String getDefaultResponse() {
        return "{" +
                "\"technical_skills\":[]," +
                "\"soft_skills\":[]," +
                "\"tools\":[]," +
                "\"certifications\":[]," +
                "\"education\":{" +
                "\"degree\":\"Unknown\"," +
                "\"field\":\"Unknown\"," +
                "\"institution\":\"Unknown\"," +
                "\"year\":2024" +
                "}," +
                "\"experience_years\":0," +
                "\"is_fresher\":true," +
                "\"summary\":\"Could not process resume.\"" +
                "}";
    }
}
