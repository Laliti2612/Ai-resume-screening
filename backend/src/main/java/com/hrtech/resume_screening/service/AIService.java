package com.hrtech.resume_screening.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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