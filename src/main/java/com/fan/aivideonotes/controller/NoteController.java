package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.NoteDto;
import com.fan.aivideonotes.controller.dto.TaskResponse;
import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.service.NoteGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteGenerationService noteGenerationService;
    private final NoteRepository noteRepository;

    @Autowired
    public NoteController(NoteGenerationService noteGenerationService, NoteRepository noteRepository) {
        this.noteGenerationService = noteGenerationService;
        this.noteRepository = noteRepository;
    }

    // 这个接口用来【创建】笔记
    @PostMapping("/generate")
    public ResponseEntity<TaskResponse> generateNotes(@RequestBody VideoLinkRequest request) {
        if (request.getUserId() == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body(new TaskResponse("User ID and video URL are required.", null));
        }
        noteGenerationService.generateNotesForVideo(request.getUserId(), request.getUrl(), request.getMode());
        String taskId = UUID.randomUUID().toString();
        return ResponseEntity.ok(new TaskResponse("Note generation task started in " + request.getMode() + " mode.", taskId));
    }

    // 这个【新】接口用来【查询】笔记
    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long noteId) { // 返回类型改为 NoteDto
        return noteRepository.findById(noteId)
                .map(note -> { // 使用 .map 进行转换
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