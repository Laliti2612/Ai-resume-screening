package com.hrtech.resume_screening.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = false, nullable = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "total_experience")
    private Double totalExperience = 0.0;

    @Column(name = "total_score")
    private Double totalScore = 0.0;

    @Column(name = "status")
    private String status = "NEW";

    @Column(name = "created_by_email")
    private String createdByEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "candidate",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference
    private List<Resume> resumes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String n) { this.fullName = n; }

    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }

    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String l) { this.linkedinUrl = l; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String g) { this.githubUrl = g; }

    public Double getTotalExperience() { return totalExperience; }
    public void setTotalExperience(Double t) { this.totalExperience = t; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double t) { this.totalScore = t; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String e) { this.createdByEmail = e; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    public List<Resume> getResumes() { return resumes; }
    public void setResumes(List<Resume> r) { this.resumes = r; }
}