package com.hrtech.resume_screening.service;

import com.hrtech.resume_screening.entity.Job;
import com.hrtech.resume_screening.repository.JobRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByEmail(String email) {
        if (email == null || email.isBlank() ||
                email.equals("unknown")) {
            return jobRepository.findAll();
        }
        return jobRepository.findByCreatedByEmail(email);
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}