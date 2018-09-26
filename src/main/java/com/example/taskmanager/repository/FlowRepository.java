package com.example.taskmanager.repository;

import com.example.taskmanager.model.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FlowRepository extends JpaRepository<Flow, Long> {
}

