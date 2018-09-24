package com.example.taskmanager.controller;

import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Job;
import com.example.taskmanager.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/job")
    public Page<Job> getJob(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    @PostMapping("/job")
    public Job createJob(@Valid @RequestBody Job job) {
        return jobRepository.save(job);
    }

    @PutMapping("/job/{jobId}")
    public Job updateJob(@PathVariable Long jobId,
                           @Valid @RequestBody Job jobRequest) {
        return jobRepository.findById(jobId)
                .map(job -> {
                    job.setStatus(jobRequest.getStatus());
                    return jobRepository.save(job);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + jobId));
    }
}
