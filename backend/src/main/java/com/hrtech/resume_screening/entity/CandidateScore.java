package com.hrtech.resume_screening.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CandidateScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    @ManyToOne
    private Candidate candidate;

    @ManyToOne
    private com.hrtech.resume_screening.model.JobPosting job;
}