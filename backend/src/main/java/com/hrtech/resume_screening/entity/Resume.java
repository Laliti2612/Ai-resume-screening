package com.hrtech.resume_screening.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
public class Resume {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private Long fileSize;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(columnDefinition = "LONGTEXT")
    private String parsedText;

    @Enumerated(EnumType.STRING)
    private ParseStatus parseStatus = ParseStatus.PENDING;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "created_by_email")
    private String createdByEmail;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonBackReference
    private Candidate candidate;
}