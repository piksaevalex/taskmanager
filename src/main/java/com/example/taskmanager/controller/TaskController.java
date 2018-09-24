package com.example.taskmanager.controller;

import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/task")
    public Page<Task> getTask(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @PostMapping("/task")
    public Task createTask(@Valid @RequestBody Task task) {
        return taskRepository.save(task);
    }

    @PutMapping("/task/{taskId}")
    public Task updateTask(@PathVariable Long taskId,
                                   @Valid @RequestBody Task taskRequest) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setStatus(taskRequest.getStatus());
                    return taskRepository.save(task);
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + taskId));
    }
}
