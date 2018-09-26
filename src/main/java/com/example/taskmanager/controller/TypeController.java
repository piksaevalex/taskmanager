package com.example.taskmanager.controller;

import com.example.taskmanager.model.Type;
import com.example.taskmanager.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class TypeController {
    @Autowired
    private TypeRepository typeRepository;

    @GetMapping("/type")
    public Page<Type> getType(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }

    @PostMapping("/type")
    public Type createType(@Valid @RequestBody Type type) {
        return typeRepository.save(type);
    }
}
