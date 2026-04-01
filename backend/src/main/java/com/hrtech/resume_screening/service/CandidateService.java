package com.hrtech.resume_screening.service;

import com.hrtech.resume_screening.entity.Candidate;
import com.hrtech.resume_screening.repository.CandidateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository repository;

    public CandidateService(CandidateRepository repository) {
        this.repository = repository;
    }

    public List<Candidate> getAllCandidates(){
        return repository.findAll();
    }

    public Candidate saveCandidate(Candidate candidate){
        return repository.save(candidate);
    }

}