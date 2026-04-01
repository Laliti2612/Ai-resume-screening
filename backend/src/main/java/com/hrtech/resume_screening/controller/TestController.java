package com.hrtech.resume_screening.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrtech.resume_screening.service.AIService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/ai")
    public String testAI() {
        return aiService.testConnection();
    }

    @GetMapping("/ai-live")
    public Map<String, Object> testAILive() {
        Map<String, Object> result = new HashMap<>();

        String testResume =
                "Lalit Ingale\n" +
                        "Email: lalit@gmail.com | Phone: 9876543210\n" +
                        "Skills: Java, Spring Boot, MySQL, React, Git\n" +
                        "Education: B.Tech Computer Science, 2024\n" +
                        "Fresher - No work experience\n" +
                        "Projects: Resume Screening System using Spring Boot and React";

        try {
            // Step 1 - Raw AI call
            String rawResult = aiService.extractSkills(testResume);
            result.put("step1_rawResult", rawResult);
            result.put("step1_length", rawResult.length());
            result.put("step1_startsWithBrace", rawResult.trim().startsWith("{"));
            result.put("step1_endsWithBrace", rawResult.trim().endsWith("}"));

            // Step 2 - JSON parse
            try {
                JsonNode root = objectMapper.readTree(rawResult);
                result.put("step2_jsonParsed", true);
                result.put("step2_hasTechnicalSkills",
                        root.has("technical_skills"));
                result.put("step2_hasExperienceYears",
                        root.has("experience_years"));
                result.put("step2_hasIsFresher",
                        root.has("is_fresher"));
                result.put("step2_hasEducation",
                        root.has("education"));

                if (root.has("technical_skills")) {
                    result.put("step2_skills",
                            root.get("technical_skills").toString());
                }
                if (root.has("experience_years")) {
                    result.put("step2_expYears",
                            root.get("experience_years").asDouble());
                }
                if (root.has("is_fresher")) {
                    result.put("step2_isFresher",
                            root.get("is_fresher").asBoolean());
                }
                if (root.has("education") &&
                        root.get("education").has("degree")) {
                    result.put("step2_degree",
                            root.get("education").get("degree").asText());
                }

            } catch (Exception parseEx) {
                result.put("step2_jsonParsed", false);
                result.put("step2_parseError", parseEx.getMessage());
                result.put("step2_failedOn", rawResult);
            }

            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }

        return result;
    }
}
