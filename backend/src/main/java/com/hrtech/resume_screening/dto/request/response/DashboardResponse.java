package com.hrtech.resumescreening.dto.response;

public class DashboardResponse {

    private int totalCandidates;
    private int totalJobs;
    private int totalResumes;

    public DashboardResponse() {}

    public DashboardResponse(int totalCandidates, int totalJobs, int totalResumes) {
        this.totalCandidates = totalCandidates;
        this.totalJobs = totalJobs;
        this.totalResumes = totalResumes;
    }

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public int getTotalResumes() {
        return totalResumes;
    }

}