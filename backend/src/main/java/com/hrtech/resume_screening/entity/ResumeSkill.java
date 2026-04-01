package com.hrtech.resume_screening.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "resume_skill")
@Data
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private SkillMaster skill;

    @Column(name = "confidence")
    private Double confidence = 1.00;
}