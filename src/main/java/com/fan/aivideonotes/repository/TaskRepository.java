package com.fan.aivideonotes.repository;

import com.fan.aivideonotes.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
}
