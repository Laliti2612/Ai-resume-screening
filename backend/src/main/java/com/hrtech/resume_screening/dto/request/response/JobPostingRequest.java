package com.hrtech.resumescreening.dto.request;

public class JobPostingRequest {

    private String title;
    private String description;

    public JobPostingRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}