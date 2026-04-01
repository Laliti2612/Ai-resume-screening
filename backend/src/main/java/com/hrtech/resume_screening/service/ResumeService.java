package com.hrtech.resume_screening.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrtech.resume_screening.entity.Candidate;
import com.hrtech.resume_screening.entity.Job;
import com.hrtech.resume_screening.entity.ParseStatus;
import com.hrtech.resume_screening.entity.Resume;
import com.hrtech.resume_screening.repository.CandidateRepository;
import com.hrtech.resume_screening.repository.JobRepository;
import com.hrtech.resume_screening.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
@Slf4j
public class ResumeService {

    private final ResumeRepository          resumeRepository;
    private final CandidateRepository       candidateRepository;
    private final JobRepository             jobRepository;
    private final ResumeParserService       resumeParserService;
    private final CandidateExtractorService candidateExtractorService;
    private final AIService                 aiService;
    private final SkillSaverService         skillSaverService;
    private final ObjectMapper              objectMapper =
            new ObjectMapper();

    // ── Universal Skill Synonyms ──────────────────────────────
    // Works for ANY technology stack
    private static final Map<String, List<String>> SYNONYMS =
            new HashMap<>() {{
                put("javascript",
                        List.of("js", "ecmascript", "es6", "es7"));
                put("typescript",
                        List.of("ts"));
                put("spring boot",
                        List.of("spring", "springboot",
                                "spring-boot", "spring framework"));
                put("reactjs",
                        List.of("react", "react.js", "react js"));
                put("nodejs",
                        List.of("node", "node.js", "node js"));
                put("postgresql",
                        List.of("postgres", "psql"));
                put("mongodb",
                        List.of("mongo"));
                put("kubernetes",
                        List.of("k8s"));
                put("rest apis",
                        List.of("rest", "restful", "rest api",
                                "restapi", "web api"));
                put("hibernate",
                        List.of("jpa", "spring data jpa",
                                "spring data"));
                put("machine learning",
                        List.of("ml", "deep learning"));
                put("artificial intelligence",
                        List.of("ai", "nlp",
                                "natural language processing"));
                put("amazon web services",
                        List.of("aws", "amazon cloud"));
                put("google cloud",
                        List.of("gcp", "google cloud platform"));
                put("microsoft azure",
                        List.of("azure"));
                put("ci/cd",
                        List.of("devops", "continuous integration",
                                "continuous delivery",
                                "jenkins", "gitlab ci",
                                "github actions", "circleci"));
                put("microservices",
                        List.of("micro services",
                                "micro-services",
                                "service oriented",
                                "soa"));
                put("vuejs",
                        List.of("vue", "vue.js", "vue js"));
                put("angular",
                        List.of("angularjs", "angular.js",
                                "angular js"));
                put("dotnet",
                        List.of(".net", "asp.net", "c#",
                                "csharp", "dot net"));
                put("python",
                        List.of("py", "python3", "python2"));
                put("django",
                        List.of("django rest framework", "drf"));
                put("flask",
                        List.of("flask api"));
                put("mysql",
                        List.of("my sql", "mariadb"));
                put("sql server",
                        List.of("mssql", "microsoft sql",
                                "t-sql"));
                put("android",
                        List.of("android development",
                                "android sdk", "kotlin android"));
                put("ios",
                        List.of("swift", "xcode",
                                "ios development"));
                put("flutter",
                        List.of("dart", "flutter sdk"));
                put("react native",
                        List.of("rn", "react-native"));
                put("linux",
                        List.of("unix", "ubuntu",
                                "centos", "bash"));
                put("git",
                        List.of("github", "gitlab",
                                "bitbucket", "version control"));
                put("docker",
                        List.of("containerization",
                                "container", "dockerfile"));
                put("terraform",
                        List.of("infrastructure as code",
                                "iac", "ansible"));
                put("elasticsearch",
                        List.of("elastic search", "elk",
                                "kibana", "logstash"));
                put("redis",
                        List.of("redis cache", "cache"));
                put("rabbitmq",
                        List.of("message queue", "kafka",
                                "apache kafka", "activemq"));
                put("graphql",
                        List.of("graph ql"));
                put("selenium",
                        List.of("test automation",
                                "automated testing"));
                put("junit",
                        List.of("unit testing", "mockito",
                                "test driven", "tdd"));
                put("data science",
                        List.of("data analysis",
                                "data analytics", "pandas",
                                "numpy", "matplotlib"));
                put("tableau",
                        List.of("power bi", "data visualization",
                                "bi tools"));
                put("blockchain",
                        List.of("ethereum", "solidity", "web3",
                                "smart contracts", "defi"));
                put("cybersecurity",
                        List.of("security", "penetration testing",
                                "ethical hacking", "owasp"));
            }};

    public ResumeService(
            ResumeRepository resumeRepository,
            CandidateRepository candidateRepository,
            JobRepository jobRepository,
            ResumeParserService resumeParserService,
            CandidateExtractorService candidateExtractorService,
            AIService aiService,
            SkillSaverService skillSaverService) {
        this.resumeRepository          = resumeRepository;
        this.candidateRepository       = candidateRepository;
        this.jobRepository             = jobRepository;
        this.resumeParserService       = resumeParserService;
        this.candidateExtractorService = candidateExtractorService;
        this.aiService                 = aiService;
        this.skillSaverService         = skillSaverService;
    }

    // ─────────────────────────────────────────────────────────
    // MAIN UPLOAD
    // ─────────────────────────────────────────────────────────
    public Map<String, Object> uploadResume(
            MultipartFile file, Long jobId,
            String userEmail) throws Exception {

        log.info("╔══════════════════════════════════╗");
        log.info("║    RESUME SCREENING STARTED      ║");
        log.info("╚══════════════════════════════════╝");
        log.info("File: {} | Job: {} | User: {}",
                file.getOriginalFilename(),
                jobId, userEmail);

        // Step 1 — Load job
        Job job = null;
        if (jobId != null && jobId > 0)
            job = jobRepository
                    .findById(jobId).orElse(null);

        if (job != null) {
            log.info("Job: {} | Exp: {}-{} | Skills: {}",
                    job.getTitle(),
                    job.getExperienceMin(),
                    job.getExperienceMax(),
                    job.getRequiredSkills());
        } else {
            log.warn("No job found for id={}", jobId);
        }

        // Step 2 — Parse resume
        String parsedText =
                resumeParserService.parseResume(file);
        if (parsedText == null || parsedText.isBlank())
            throw new Exception(
                    "Could not extract text from resume");
        log.info("Parsed: {} chars", parsedText.length());

        // Step 3 — Extract candidate
        Candidate extracted =
                candidateExtractorService
                        .extractCandidate(parsedText);
        log.info("Name: {} | Email: {}",
                extracted.getFullName(),
                extracted.getEmail());

        // Step 4 — Find or create
        Candidate candidate =
                findOrCreateCandidate(extracted, userEmail);

        // Step 5 — AI extraction
        log.info("Calling AI...");
        String aiResult =
                aiService.extractSkills(parsedText);
        String cleanedJson = cleanJson(aiResult);
        log.info("AI JSON: {}", cleanedJson);

        // Step 6 — Save resume
        Resume resume = new Resume();
        resume.setFileName(file.getOriginalFilename());
        resume.setFileSize(file.getSize());
        resume.setFileData(file.getBytes());
        resume.setParsedText(parsedText);
        resume.setParseStatus(ParseStatus.COMPLETED);
        resume.setCandidate(candidate);
        resume.setCreatedByEmail(userEmail);
        resumeRepository.save(resume);

        // Step 7 — Save skills
        skillSaverService.saveSkills(cleanedJson, resume);

        // Step 8 — Score
        Map<String, Object> scoreData =
                runScoringEngine(
                        cleanedJson, parsedText,
                        candidate, job, userEmail);

        log.info("╔══════════════════════════════════╗");
        log.info("║ {} | Score: {}",
                candidate.getStatus(),
                scoreData.get("totalScore"));
        log.info("╚══════════════════════════════════╝");

        Map<String, Object> result = new HashMap<>();
        result.put("candidateId",
                candidate.getId());
        result.put("candidateName",
                candidate.getFullName());
        result.put("score",     scoreData);
        result.put("recommendation",
                candidate.getStatus());
        result.put("aiSummary",
                scoreData.get("aiSummary"));
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // SCORING ENGINE
    // Dynamic — works for ANY job role / tech stack
    // ─────────────────────────────────────────────────────────
    private Map<String, Object> runScoringEngine(
            String aiJson, String parsedText,
            Candidate candidate, Job job,
            String userEmail) {

        Map<String, Object> scoreData = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(aiJson);

            // ── Extract data from AI ──────────────────────────
            boolean isFresher = !root.has("is_fresher")
                    || root.get("is_fresher")
                    .asBoolean(true);
            double expYears =
                    root.has("experience_years")
                            ? root.get("experience_years")
                            .asDouble(0)
                            : 0;
            if (isFresher) expYears = 0;
            candidate.setTotalExperience(expYears);

            // Skills from resume
            List<String> rawSkills = new ArrayList<>();
            addSkillsFromNode(root,
                    "technical_skills", rawSkills);
            addSkillsFromNode(root, "tools", rawSkills);
            List<String> skills =
                    normalizeSkills(rawSkills);

            // Soft skills
            List<String> softSkills = new ArrayList<>();
            addSkillsFromNode(root,
                    "soft_skills", softSkills);

            // Certifications
            List<String> certs = new ArrayList<>();
            addSkillsFromNode(root,
                    "certifications", certs);

            // Education
            String degree = "";
            String field  = "";
            if (root.has("education")) {
                JsonNode edu = root.get("education");
                degree = edu.has("degree")
                        ? edu.get("degree").asText("")
                        .toLowerCase()
                        : "";
                field  = edu.has("field")
                        ? edu.get("field").asText("")
                        .toLowerCase()
                        : "";
            }

            String summary = root.has("summary")
                    ? root.get("summary").asText("") : "";

            log.info("━━━━━━━━━━ EXTRACTED DATA ━━━━━━━━");
            log.info("Fresher : {} | Exp: {} yrs",
                    isFresher, expYears);
            log.info("Skills  : {}", skills);
            log.info("Certs   : {}", certs);
            log.info("Degree  : {} in {}", degree, field);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // ── Compute all scores ────────────────────────────
            // All scores are based purely on
            // job requirements — no hardcoded skill lists

            SkillMatchResult skillMatch =
                    computeSkillMatch(skills, job);

            double expScore =
                    computeExperienceScore(
                            expYears, isFresher, job);

            double eduScore =
                    computeEducationScore(degree, field);

            double similarityScore =
                    computeJDSimilarity(
                            parsedText, job, skills);

            ProjectAnalysis projects =
                    analyzeProjects(parsedText, job);

            double certScore =
                    computeCertScore(certs, job);

            double softScore =
                    computeSoftSkillScore(softSkills);

            log.info("━━━━━━━━━━━━ SCORES ━━━━━━━━━━━━━━");
            log.info("Skill     : {} ({}/{}  {}%)",
                    skillMatch.score,
                    skillMatch.matchedCount,
                    skillMatch.totalRequired,
                    skillMatch.matchPercent);
            log.info("Exp       : {}", expScore);
            log.info("Education : {}", eduScore);
            log.info("Similarity: {}", similarityScore);
            log.info("Projects  : {} ({})",
                    projects.score, projects.count);
            log.info("Certs     : {}", certScore);
            log.info("Soft      : {}", softScore);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // ── Choose formula based on exp level ─────────────
            double total;
            String formula;
            Map<String, Double> weights =
                    new LinkedHashMap<>();

            if (isFresher || expYears < 1) {
                // FRESHER: Skills + Projects matter most
                // No full-time exp → don't penalize for it
                weights.put("skills",     0.40);
                weights.put("projects",   0.25);
                weights.put("education",  0.15);
                weights.put("similarity", 0.10);
                weights.put("certs",      0.05);
                weights.put("soft",       0.05);

                total = (skillMatch.score * 0.40)
                        + (projects.score   * 0.25)
                        + (eduScore         * 0.15)
                        + (similarityScore  * 0.10)
                        + (certScore        * 0.05)
                        + (softScore        * 0.05);

                formula = "FRESHER";

            } else if (expYears <= 3) {
                // JUNIOR (1-3 yrs): Skills still primary
                weights.put("skills",     0.40);
                weights.put("experience", 0.20);
                weights.put("projects",   0.15);
                weights.put("education",  0.10);
                weights.put("similarity", 0.10);
                weights.put("certs",      0.05);

                total = (skillMatch.score * 0.40)
                        + (expScore         * 0.20)
                        + (projects.score   * 0.15)
                        + (eduScore         * 0.10)
                        + (similarityScore  * 0.10)
                        + (certScore        * 0.05);

                formula = "JUNIOR_1_3_YRS";

            } else if (expYears <= 7) {
                // MID-LEVEL (3-7 yrs): Balance
                weights.put("skills",     0.35);
                weights.put("experience", 0.30);
                weights.put("similarity", 0.15);
                weights.put("education",  0.10);
                weights.put("projects",   0.05);
                weights.put("certs",      0.05);

                total = (skillMatch.score * 0.35)
                        + (expScore         * 0.30)
                        + (similarityScore  * 0.15)
                        + (eduScore         * 0.10)
                        + (projects.score   * 0.05)
                        + (certScore        * 0.05);

                formula = "MID_LEVEL_3_7_YRS";

            } else {
                // SENIOR (7+ yrs): Experience matters most
                weights.put("experience", 0.35);
                weights.put("skills",     0.30);
                weights.put("similarity", 0.20);
                weights.put("education",  0.10);
                weights.put("certs",      0.05);

                total = (expScore         * 0.35)
                        + (skillMatch.score * 0.30)
                        + (similarityScore  * 0.20)
                        + (eduScore         * 0.10)
                        + (certScore        * 0.05);

                formula = "SENIOR_7_PLUS_YRS";
            }

            total = Math.round(total * 10.0) / 10.0;
            log.info("Formula: {} | Raw Score: {}",
                    formula, total);

            // ── Experience Gate ───────────────────────────────
            // Hard rules — prevents wrong shortlisting
            // These apply to ANY job role
            String gateReason = null;

            if (job != null) {
                int minExp =
                        job.getExperienceMin() != null
                                ? job.getExperienceMin() : 0;
                int maxExp =
                        job.getExperienceMax() != null
                                ? job.getExperienceMax() : 99;

                log.info("Gate: minExp={} expYears={}",
                        minExp, expYears);

                // GATE 1: Fresher → any job needing 2+ yrs
                // Hard cap → REJECTED always
                if (minExp >= 2
                        && (isFresher || expYears < 1)) {
                    total = 30.0;
                    gateReason =
                            "Fresher applied for " +
                                    minExp + "+ yr role";
                    log.info("GATE 1: {}", gateReason);

                    // GATE 2: Exp < 50% of required
                    // → definitely not ready
                } else if (minExp >= 1
                        && expYears < minExp * 0.5) {
                    double cap = 30.0 +
                            (expYears / minExp) * 14.0;
                    total = Math.min(total,
                            Math.round(cap * 10.0) / 10.0);
                    gateReason =
                            "Exp " + expYears +
                                    " yrs < 50% of required " +
                                    minExp + " yrs";
                    log.info("GATE 2: {}", gateReason);

                    // GATE 3: Exp < 75% of required
                    // → max Under Review, not Shortlisted
                } else if (minExp >= 1
                        && expYears < minExp * 0.75) {
                    total = Math.min(total, 64.0);
                    gateReason =
                            "Exp " + expYears +
                                    " yrs < 75% of required " +
                                    minExp + " yrs";
                    log.info("GATE 3: {}", gateReason);
                }

                // GATE 4: Skill match < 30%
                // → not enough relevant skills
                if (gateReason == null
                        && skillMatch.matchPercent < 30
                        && !job.getRequiredSkills()
                        .isBlank()) {
                    total = Math.min(total, 34.0);
                    gateReason =
                            "Skill match only " +
                                    skillMatch.matchPercent + "%";
                    log.info("GATE 4: {}", gateReason);
                }
            }

            // ── Bonus points ──────────────────────────────────
            double bonus = 0;
            if (gateReason == null) {
                // All required skills matched
                if (skillMatch.matchPercent >= 90)
                    bonus += 2.0;
                // Relevant certifications
                bonus += Math.min(certs.size(), 3) * 1.0;
                // Good soft skills
                if (softScore >= 70) bonus += 1.0;
                // Extra projects
                if (projects.count >= 3) bonus += 1.0;
                // Cap bonus at 5
                bonus = Math.min(bonus, 5.0);
                total = Math.min(100.0, total + bonus);
            }

            total = Math.round(total * 10.0) / 10.0;
            log.info("Final Score (after gate+bonus): {}",
                    total);

            // ── Determine status ──────────────────────────────
            String status;
            String statusReason;

            if (total >= 80) {
                status = "SHORTLISTED";
                statusReason =
                        "Excellent match — ready for interview";
            } else if (total >= 65) {
                status = "SHORTLISTED";
                statusReason =
                        "Good match — worth interviewing";
            } else if (total >= 50) {
                status = "NEW";
                statusReason =
                        "Partial match — needs manual review";
            } else if (total >= 35) {
                status = "NEW";
                statusReason =
                        "Below average — significant gaps";
            } else {
                status = "REJECTED";
                statusReason = gateReason != null
                        ? "Rejected: " + gateReason
                        : "Poor match — does not qualify";
            }

            // Critical override:
            // < 30% skill match → always REJECTED
            if (job != null
                    && job.getRequiredSkills() != null
                    && !job.getRequiredSkills().isBlank()
                    && skillMatch.matchPercent < 30) {
                status = "REJECTED";
                statusReason =
                        "Critical: Only " +
                                skillMatch.matchPercent +
                                "% skill match";
                total = Math.min(total, 34.0);
            }

            log.info("STATUS: {} | {}",
                    status, statusReason);

            // ── Save candidate ────────────────────────────────
            candidate.setTotalScore(total);
            candidate.setStatus(status);
            candidate.setCreatedByEmail(userEmail);
            candidateRepository.save(candidate);

            // ── Build response ────────────────────────────────
            scoreData.put("totalScore",      total);
            scoreData.put("skillScore",
                    Math.round(skillMatch.score * 10.0) / 10.0);
            scoreData.put("experienceScore",
                    Math.round(expScore        * 10.0) / 10.0);
            scoreData.put("educationScore",
                    Math.round(eduScore        * 10.0) / 10.0);
            scoreData.put("keywordScore",
                    Math.round(similarityScore * 10.0) / 10.0);
            scoreData.put("projectScore",
                    Math.round(projects.score  * 10.0) / 10.0);
            scoreData.put("certScore",
                    Math.round(certScore       * 10.0) / 10.0);
            scoreData.put("softSkillScore",
                    Math.round(softScore       * 10.0) / 10.0);
            scoreData.put("skillMatchPercent",
                    skillMatch.matchPercent);
            scoreData.put("extractedSkills",  skills);
            scoreData.put("missingSkills",
                    skillMatch.missingSkills);
            scoreData.put("matchedSkills",
                    skillMatch.matchedSkills);
            scoreData.put("aiSummary",        summary);
            scoreData.put("recommendation",   status);
            scoreData.put("statusReason",     statusReason);
            scoreData.put("formulaUsed",      formula);
            scoreData.put("isFresher",        isFresher);
            scoreData.put("experienceYears",  expYears);
            scoreData.put("bonusPoints",      bonus);
            scoreData.put("gateReason",       gateReason);
            scoreData.put("projectCount",     projects.count);
            scoreData.put("weights",          weights);

        } catch (Exception e) {
            log.error("Scoring failed: {} | {}",
                    e.getClass().getSimpleName(),
                    e.getMessage());
            e.printStackTrace();

            candidate.setTotalScore(0.0);
            candidate.setStatus("NEW");
            candidate.setCreatedByEmail(userEmail);
            candidateRepository.save(candidate);

            scoreData.put("totalScore",      0.0);
            scoreData.put("skillScore",      0.0);
            scoreData.put("experienceScore", 0.0);
            scoreData.put("educationScore",  0.0);
            scoreData.put("keywordScore",    0.0);
            scoreData.put("projectScore",    0.0);
            scoreData.put("certScore",       0.0);
            scoreData.put("softSkillScore",  0.0);
            scoreData.put("skillMatchPercent", 0);
            scoreData.put("extractedSkills", new ArrayList<>());
            scoreData.put("missingSkills",   new ArrayList<>());
            scoreData.put("matchedSkills",   new ArrayList<>());
            scoreData.put("aiSummary",       "");
            scoreData.put("recommendation",  "NEW");
            scoreData.put("statusReason",    "Error");
            scoreData.put("formulaUsed",     "ERROR");
        }

        return scoreData;
    }

    // ─────────────────────────────────────────────────────────
    // SKILL MATCH — works for ANY tech stack
    // Reads skills purely from job.getRequiredSkills()
    // ─────────────────────────────────────────────────────────
    private static class SkillMatchResult {
        double       score;
        int          matchedCount;
        int          totalRequired;
        int          matchPercent;
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
    }

    private SkillMatchResult computeSkillMatch(
            List<String> candidateSkills, Job job) {

        SkillMatchResult result = new SkillMatchResult();

        // No job defined — score by skill count
        if (job == null
                || job.getRequiredSkills() == null
                || job.getRequiredSkills().isBlank()) {
            result.score        =
                    Math.min(100.0,
                            candidateSkills.size() * 8.0);
            result.matchedCount  = candidateSkills.size();
            result.totalRequired = candidateSkills.size();
            result.matchPercent  = 100;
            result.matchedSkills = candidateSkills;
            return result;
        }

        // Parse required skills from job
        String[] required = job.getRequiredSkills()
                .toLowerCase().split(",");
        result.totalRequired = 0;

        for (String req : required) {
            String r = req.trim();
            if (r.isEmpty()) continue;
            result.totalRequired++;

            String rResolved = resolveSkill(r);
            boolean found = false;

            for (String cand : candidateSkills) {
                String cResolved = resolveSkill(cand);

                // Exact match
                if (rResolved.equals(cResolved)) {
                    found = true;
                    break;
                }
                // Partial match (substring)
                if (rResolved.contains(cResolved)
                        || cResolved.contains(rResolved)) {
                    found = true;
                    break;
                }
                // Original match
                if (r.contains(cand) || cand.contains(r)) {
                    found = true;
                    break;
                }
            }

            if (found)
                result.matchedSkills.add(r);
            else
                result.missingSkills.add(r);
        }

        result.matchedCount = result.matchedSkills.size();
        result.matchPercent = result.totalRequired == 0
                ? 100
                : (int) Math.round(
                (double) result.matchedCount
                        / result.totalRequired * 100);

        // Base score = match percentage
        result.score = result.matchPercent;

        // Bonus for extra skills beyond required
        int extraSkills = Math.max(0,
                candidateSkills.size()
                        - result.matchedCount);
        if (extraSkills > 0) {
            result.score = Math.min(100.0,
                    result.score +
                            Math.min(extraSkills * 2.0, 10.0));
        }

        log.info("Skill: {}/{} = {}% | Matched: {} | " +
                        "Missing: {}",
                result.matchedCount,
                result.totalRequired,
                result.matchPercent,
                result.matchedSkills,
                result.missingSkills);

        return result;
    }

    // ─────────────────────────────────────────────────────────
    // EXPERIENCE SCORE
    // Formula: min(candidateExp/requiredExp, 1) × 100
    // ─────────────────────────────────────────────────────────
    private double computeExperienceScore(
            double expYears,
            boolean isFresher,
            Job job) {

        // No job — general scoring
        if (job == null) {
            if (isFresher || expYears == 0) return 50.0;
            if (expYears <= 2)  return 65.0;
            if (expYears <= 5)  return 80.0;
            if (expYears <= 8)  return 90.0;
            return 100.0;
        }

        int minExp = job.getExperienceMin() != null
                ? job.getExperienceMin() : 0;
        int maxExp = job.getExperienceMax() != null
                ? job.getExperienceMax() : 10;

        log.info("Exp needed: {}-{} | Candidate: {} yrs",
                minExp, maxExp, expYears);

        // Entry-level job — all welcome
        if (minExp == 0) {
            if (isFresher || expYears == 0) return 100.0;
            if (expYears <= 2)              return 90.0;
            return 80.0;
        }

        // Fresher for experienced role → very low
        if (isFresher || expYears == 0) return 5.0;

        // Perfect range match → full score
        if (expYears >= minExp && expYears <= maxExp)
            return 100.0;

        // Under-experienced
        // Formula: min(candidate/required, 1) × 100
        if (expYears < minExp) {
            double ratio = expYears / (double) minExp;
            double score = ratio * 100.0;
            return Math.max(10.0,
                    Math.round(score * 10.0) / 10.0);
        }

        // Over-experienced (small penalty — not a dealbreaker)
        double overBy = expYears - maxExp;
        if (overBy <= 2)  return 90.0;
        if (overBy <= 5)  return 80.0;
        return 70.0;
    }

    // ─────────────────────────────────────────────────────────
    // EDUCATION SCORE
    // ─────────────────────────────────────────────────────────
    private double computeEducationScore(
            String degree, String field) {

        double score;

        if (degree.contains("phd")
                || degree.contains("doctorate"))
            score = 100.0;
        else if (degree.contains("master")
                || degree.contains("mtech")
                || degree.contains("m.tech")
                || degree.contains("mca")
                || degree.contains("msc")
                || degree.contains("me"))
            score = 88.0;
        else if (degree.contains("bachelor")
                || degree.contains("btech")
                || degree.contains("b.tech")
                || degree.contains("be")
                || degree.contains("b.e")
                || degree.contains("bsc")
                || degree.contains("bca"))
            score = 75.0;
        else if (degree.contains("diploma"))
            score = 55.0;
        else
            score = 45.0;

        // Field relevance bonus — works for any domain
        // Technical fields get a small boost
        if (field.contains("computer")
                || field.contains("software")
                || field.contains("information")
                || field.contains("cse")
                || field.contains("engineering")
                || field.contains("science")
                || field.contains("technology")) {
            score = Math.min(100.0, score + 5.0);
        }

        log.info("Education: {} in {} → {}",
                degree, field, score);
        return score;
    }

    // ─────────────────────────────────────────────────────────
    // JD SIMILARITY — keyword overlap with frequency boost
    // Compares resume text with job required skills
    // ─────────────────────────────────────────────────────────
    private double computeJDSimilarity(
            String parsedText, Job job,
            List<String> candidateSkills) {

        if (job == null
                || job.getRequiredSkills() == null
                || job.getRequiredSkills().isBlank()) {
            return Math.min(100.0,
                    candidateSkills.size() * 7.0);
        }

        String lowerText = parsedText.toLowerCase();
        String[] keywords = job.getRequiredSkills()
                .toLowerCase().split(",");

        int found = 0;
        int total = 0;

        for (String kw : keywords) {
            String k = kw.trim();
            if (k.isEmpty()) continue;
            total++;

            int occurrences = countOccurrences(lowerText, k);
            if (occurrences > 0) {
                found++;
                // Frequency bonus — mentioned 3+ times
                if (occurrences >= 3) found++;
            }
        }

        if (total == 0) return 50.0;

        double score = Math.min(100.0,
                (double) found / total * 100.0);
        log.info("JD Similarity: {}/{} = {}",
                found, total, score);
        return score;
    }

    // ─────────────────────────────────────────────────────────
    // PROJECT ANALYSIS — works for any domain
    // ─────────────────────────────────────────────────────────
    private static class ProjectAnalysis {
        int    count = 0;
        double score = 0;
    }

    private ProjectAnalysis analyzeProjects(
            String parsedText, Job job) {

        ProjectAnalysis result = new ProjectAnalysis();
        if (parsedText == null) return result;

        String lower = parsedText.toLowerCase();

        // Count project mentions
        int projectCount = 0;
        String[] lines = parsedText.split("\n");
        boolean inProjectSection = false;

        for (String line : lines) {
            String l = line.trim().toLowerCase();

            // Detect project section header
            if (l.contains("project") && l.length() < 30) {
                inProjectSection = true;
                continue;
            }

            // End of project section
            if (inProjectSection && (
                    l.contains("experience") ||
                            l.contains("education") ||
                            l.contains("skill") ||
                            l.contains("certification") ||
                            l.contains("achievement"))) {
                inProjectSection = false;
            }

            // Count project entries
            if (inProjectSection && line.trim().length() > 10
                    && (line.trim().startsWith("•")
                    || line.trim().startsWith("-")
                    || line.trim().startsWith("*")
                    || (line.trim().length() > 5
                    && Character.isUpperCase(
                    line.trim().charAt(0))
                    && !line.trim().endsWith(":")))) {
                projectCount++;
            }
        }

        // Fallback count
        if (projectCount == 0) {
            projectCount = countOccurrences(
                    lower, "project");
            projectCount = Math.min(projectCount, 5);
        }

        result.count = Math.max(0, Math.min(projectCount, 10));

        // Score based on count
        if (result.count == 0)      result.score = 20.0;
        else if (result.count == 1) result.score = 45.0;
        else if (result.count == 2) result.score = 65.0;
        else if (result.count == 3) result.score = 80.0;
        else                        result.score = 90.0;

        // Bonus: projects use job-required skills
        if (job != null
                && job.getRequiredSkills() != null
                && !job.getRequiredSkills().isBlank()) {
            String[] required = job.getRequiredSkills()
                    .toLowerCase().split(",");
            int relevant = 0;
            for (String req : required) {
                if (lower.contains(req.trim()))
                    relevant++;
            }
            double relevanceRatio =
                    required.length > 0
                            ? (double) relevant / required.length
                            : 0;
            if (relevanceRatio >= 0.5) {
                result.score = Math.min(100.0,
                        result.score + 10.0);
            }
        }

        log.info("Projects: count={} score={}",
                result.count, result.score);
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // CERTIFICATION SCORE
    // ─────────────────────────────────────────────────────────
    private double computeCertScore(
            List<String> certs, Job job) {

        if (certs == null || certs.isEmpty())
            return 20.0;

        double score = Math.min(100.0,
                20.0 + certs.size() * 20.0);

        // Bonus for job-relevant certs
        if (job != null
                && job.getRequiredSkills() != null) {
            String jobSkills = job.getRequiredSkills()
                    .toLowerCase();
            for (String cert : certs) {
                String c = cert.toLowerCase();
                // Check if cert matches any required skill
                for (String req : jobSkills.split(",")) {
                    if (c.contains(req.trim())
                            || req.trim().contains(c)) {
                        score = Math.min(100.0,
                                score + 10.0);
                        break;
                    }
                }
            }
        }

        log.info("Cert score: {} ({})", score, certs);
        return score;
    }

    // ─────────────────────────────────────────────────────────
    // SOFT SKILL SCORE
    // ─────────────────────────────────────────────────────────
    private double computeSoftSkillScore(
            List<String> softSkills) {

        if (softSkills == null || softSkills.isEmpty())
            return 40.0;

        double score = Math.min(100.0,
                40.0 + softSkills.size() * 10.0);
        log.info("Soft score: {}", score);
        return score;
    }

    // ─────────────────────────────────────────────────────────
    // SKILL RESOLVER — maps synonyms to canonical form
    // ─────────────────────────────────────────────────────────
    private String resolveSkill(String skill) {
        if (skill == null) return "";
        String s = skill.toLowerCase().trim();

        // Check if it's a synonym → return canonical
        for (Map.Entry<String, List<String>> entry
                : SYNONYMS.entrySet()) {
            if (entry.getValue().contains(s))
                return entry.getKey();
        }

        // Check if it's already a canonical key
        if (SYNONYMS.containsKey(s)) return s;

        return s;
    }

    private List<String> normalizeSkills(
            List<String> skills) {
        List<String> normalized = new ArrayList<>();
        for (String skill : skills) {
            String resolved =
                    resolveSkill(
                            skill.toLowerCase().trim());
            if (!normalized.contains(resolved))
                normalized.add(resolved);
        }
        return normalized;
    }

    // ─────────────────────────────────────────────────────────
    // FIND OR CREATE CANDIDATE
    // ─────────────────────────────────────────────────────────
    private Candidate findOrCreateCandidate(
            Candidate extracted, String userEmail) {

        String email = extracted.getEmail();
        boolean hasEmail = email != null
                && !email.isBlank()
                && !email.equals("unknown@email.com");

        if (hasEmail) {
            Optional<Candidate> byEmailUser =
                    candidateRepository
                            .findByEmailAndCreatedByEmail(
                                    email, userEmail);
            if (byEmailUser.isPresent()) {
                Candidate c = byEmailUser.get();
                updateBasicInfo(c, extracted);
                c.setCreatedByEmail(userEmail);
                return candidateRepository.save(c);
            }

            Optional<Candidate> byEmail =
                    candidateRepository
                            .findFirstByEmail(email);
            if (byEmail.isPresent()) {
                Candidate c = byEmail.get();
                updateBasicInfo(c, extracted);
                c.setCreatedByEmail(userEmail);
                return candidateRepository.save(c);
            }
        }

        if (!hasEmail
                && extracted.getFullName() != null
                && !extracted.getFullName()
                .equals("Unknown Candidate")) {
            Optional<Candidate> byName =
                    candidateRepository
                            .findFirstByFullNameAndCreatedByEmail(
                                    extracted.getFullName(),
                                    userEmail);
            if (byName.isPresent()) {
                Candidate c = byName.get();
                updateBasicInfo(c, extracted);
                return candidateRepository.save(c);
            }
        }

        extracted.setCreatedByEmail(userEmail);
        if (extracted.getFullName() == null
                || extracted.getFullName().isBlank())
            extracted.setFullName("Unknown Candidate");

        try {
            return candidateRepository.save(extracted);
        } catch (Exception e) {
            log.warn("Insert failed: {}", e.getMessage());
            if (hasEmail) {
                return candidateRepository
                        .findFirstByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cannot save candidate"));
            }
            throw new RuntimeException(
                    "Cannot save: " + e.getMessage());
        }
    }

    private void updateBasicInfo(Candidate existing,
                                 Candidate extracted) {
        if (extracted.getFullName() != null
                && !extracted.getFullName().isBlank()
                && !extracted.getFullName()
                .equals("Unknown Candidate"))
            existing.setFullName(extracted.getFullName());

        if (extracted.getPhone() != null
                && !extracted.getPhone()
                .equals("0000000000"))
            existing.setPhone(extracted.getPhone());
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────
    private int countOccurrences(String text,
                                 String keyword) {
        if (text == null || keyword == null
                || keyword.isBlank()) return 0;
        int count = 0, idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }

    private void addSkillsFromNode(JsonNode root,
                                   String field,
                                   List<String> skills) {
        if (root.has(field) && root.get(field).isArray()) {
            for (JsonNode n : root.get(field)) {
                String s = n.asText("").toLowerCase().trim();
                if (!s.isEmpty() && !skills.contains(s))
                    skills.add(s);
            }
        }
    }

    private String cleanJson(String raw) {
        if (raw == null || raw.isBlank())
            return getEmptyJson();
        String cleaned = raw
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end   = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start)
            return cleaned.substring(start, end + 1);
        return getEmptyJson();
    }

    private String getEmptyJson() {
        return "{\"technical_skills\":[]," +
                "\"soft_skills\":[]," +
                "\"tools\":[]," +
                "\"certifications\":[]," +
                "\"education\":{" +
                "\"degree\":\"Unknown\"," +
                "\"field\":\"Unknown\"," +
                "\"institution\":\"Unknown\"," +
                "\"year\":2024}," +
                "\"experience_years\":0," +
                "\"is_fresher\":true," +
                "\"summary\":\"\"}";
    }
}