package com.hrtech.resume_screening.dto.request.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CandidateScoreResponse {

    private BigDecimal skillScore;
    private BigDecimal experienceScore;
    private BigDecimal educationScore;
    private BigDecimal keywordScore;
    private BigDecimal totalScore;

}