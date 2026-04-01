package com.hrtech.resume_screening.model;

import jakarta.persistence.*;

@Entity
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    // required skills (comma separated)
    private String requiredSkills;

    // experience range
    private double experienceMin;

    private double experienceMax;

    public JobPosting(){}

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public double getExperienceMin() {
        return experienceMin;
    }

    public double getExperienceMax() {
        return experienceMax;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public void setExperienceMin(double experienceMin) {
        this.experienceMin = experienceMin;
    }

    public void setExperienceMax(double experienceMax) {
        this.experienceMax = experienceMax;
    }
}