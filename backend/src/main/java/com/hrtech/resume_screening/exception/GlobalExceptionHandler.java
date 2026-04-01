package com.hrtech.resumescreening.exception;

import com.hrtech.resume_screening.exception.ResumeParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResumeParseException.class)
    public ResponseEntity<String> handleResumeParseException(ResumeParseException ex) {

        return ResponseEntity
                .badRequest()
                .body("Resume Parsing Error: " + ex.getMessage());
    }

    @ExceptionHandler(com.hrtech.resumescreening.exception.AIServiceException.class)
    public ResponseEntity<String> handleAIServiceException(com.hrtech.resumescreening.exception.AIServiceException ex) {

        return ResponseEntity
                .internalServerError()
                .body("AI Service Error: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {

        return ResponseEntity
                .internalServerError()
                .body("Unexpected Error: " + ex.getMessage());
    }

}