package com.example.taskmanager.repository;

import com.example.taskmanager.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {
}
