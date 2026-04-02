package com.hrtech.resume_screening.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private Long fileSize;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    @JsonIgnore   // ← NEVER send binary file data in API response
    private byte[] fileData;

    @Column(columnDefinition = "LONGTEXT")
    @JsonIgnore   // ← NEVER send full parsed text (too large)
    private String parsedText;

    @Enumerated(EnumType.STRING)
    private ParseStatus parseStatus = ParseStatus.PENDING;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "created_by_email")
    private String createdByEmail;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonBackReference   // ← breaks Candidate → Resume → Candidate loop
    private Candidate candidate;

    @OneToMany(mappedBy = "resume",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore   // ← don't nest skills inside resume inside candidate
    private List<ResumeSkill> skills;

    // ================================================================
    // Getters & Setters
    // ================================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String f) { this.fileName = f; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long f) { this.fileSize = f; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] f) { this.fileData = f; }

    public String getParsedText() { return parsedText; }
    public void setParsedText(String p) { this.parsedText = p; }

    public ParseStatus getParseStatus() { return parseStatus; }
    public void setParseStatus(ParseStatus p) { this.parseStatus = p; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime u) { this.uploadedAt = u; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String e) { this.createdByEmail = e; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate c) { this.candidate = c; }

    public List<ResumeSkill> getSkills() { return skills; }
    public void setSkills(List<ResumeSkill> s) { this.skills = s; }
}
