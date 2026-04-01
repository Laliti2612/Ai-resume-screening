package com.hrtech.resume_screening.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "skill_master")
@Data
public class SkillMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_name", unique = true, nullable = false)
    private String skillName;

    private String category;
}