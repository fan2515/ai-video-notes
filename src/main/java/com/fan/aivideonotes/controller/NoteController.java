package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.NoteDto;
import com.fan.aivideonotes.controller.dto.TaskResponse;
import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.repository.TaskRepository;
import com.fan.aivideonotes.service.NoteGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes") // 基础路径保持不变，代表“笔记”相关的操作
public class NoteController {

    private final NoteGenerationService noteGenerationService;
    private final TaskRepository taskRepository;

    private final NoteRepository noteRepository;

    @Autowired
    public NoteController(NoteGenerationService noteGenerationService, TaskRepository taskRepository, NoteRepository noteRepository) {
        this.noteGenerationService = noteGenerationService;
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<TaskResponse> generateNotes(@RequestBody VideoLinkRequest request) {
        if (request.getUserId() == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body(new TaskResponse("User ID and video URL are required.", null));
        }

        String taskId = UUID.randomUUID().toString();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus("PENDING");
        task.setStatusMessage("Task has been queued for processing.");
        taskRepository.save(task);

        noteGenerationService.generateNotesForVideo(taskId, request.getUserId(), request.getUrl(), request.getMode());

        return ResponseEntity.ok(new TaskResponse("Note generation task created successfully.", taskId));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long noteId) {
        return noteRepository.findById(noteId)
                .map(note -> {
                    NoteDto dto = new NoteDto();
                    dto.setId(note.getId());
                    dto.setVideoUrl(note.getVideoUrl());
                    dto.setContent(note.getContent());
                    dto.setCreatedAt(note.getCreatedAt());
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}