package com.hrtech.resume_screening.dto.request.response;

public class ResumeUploadRequest {


    private Long candidateId;
    private String fileName;

    public ResumeUploadRequest() {}

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}