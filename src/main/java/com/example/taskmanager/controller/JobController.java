package com.example.taskmanager.controller;

import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Flow;
import com.example.taskmanager.model.Job;
import com.example.taskmanager.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/job")
    public Page<Job> getJob(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    @GetMapping("/job/{taskId}/jobs")
    public List<Job> getJobbyId(@PathVariable Long taskId) {
        return jobRepository.findByTaskId(taskId);
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
                    Flow flow = new Flow();
                    if (jobRequest.getStatus() == Job.Status.DONE){
                        flow.deleteBranch(jobId,true);
                    }
                    if (jobRequest.getStatus() == Job.Status.REJECTED){
                        flow.deleteBranch(jobId,false);
                    }
                    return jobRepository.save(job);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + jobId));
    }
}
