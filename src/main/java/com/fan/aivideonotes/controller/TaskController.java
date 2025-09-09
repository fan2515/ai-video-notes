package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<Task> getTaskStatus(@PathVariable String taskId) {
        return taskRepository.findById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{taskId}/result")
    public ResponseEntity<Object> getTaskResult(@PathVariable String taskId) {
        // 1. 先从数据库查找 Task
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        // 2. 判断 Task 是否存在
        if (taskOptional.isEmpty()) {
            // 如果不存在，直接返回 404
            return ResponseEntity.notFound().build();
        }

        // 3. 如果存在，获取 Task 对象
        Task task = taskOptional.get();

        // 4. 根据 Task 的状态，决定返回什么内容
        if ("COMPLETED".equals(task.getStatus()) && task.getResultNote() != null) {
            // 如果已完成，返回 Note
            return ResponseEntity.ok(task.getResultNote());
        } else {
            // 如果未完成或失败，返回 Task 状态本身
            return ResponseEntity.ok(task);
        }
    }
}