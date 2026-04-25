package com.hrtech.resume_screening.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "experience_min")
    private Integer experienceMin = 0;

    @Column(name = "experience_max")
    private Integer experienceMax = 10;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "created_by_email")
    private String createdByEmail;

    // ── NEW: unique public share token ─────────────────────────
    @Column(name = "public_id", unique = true)
    private String publicId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Job() {
        // Auto-generate publicId when job is created
        this.publicId = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12);
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }

    public String getLocation() { return location; }
    public void setLocation(String l) { this.location = l; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String s) { this.requiredSkills = s; }

    public Integer getExperienceMin() { return experienceMin; }
    public void setExperienceMin(Integer e) { this.experienceMin = e; }

    public Integer getExperienceMax() { return experienceMax; }
    public void setExperienceMax(Integer e) { this.experienceMax = e; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String e) { this.createdByEmail = e; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String p) { this.publicId = p; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
