package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.TaskResponse;
import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.service.NoteGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteGenerationService noteGenerationService;

    @Autowired
    public NoteController(NoteGenerationService noteGenerationService) {
        this.noteGenerationService = noteGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TaskResponse> generateNotes(@RequestBody VideoLinkRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(new TaskResponse("User ID is required.", null));
        }

        // 异步调用服务
        noteGenerationService.generateNotesForVideo(request.getUserId());

        String taskId = UUID.randomUUID().toString();
        return ResponseEntity.ok(new TaskResponse("Note generation task started.", taskId));
    }
}
