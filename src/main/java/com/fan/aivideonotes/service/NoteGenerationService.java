// NoteGenerationService.java
package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NoteGenerationService {

    private final AiServiceProvider geminiProvider;
    private final NoteRepository noteRepository;
    private final VideoProcessingService videoProcessingService;

    @Autowired
    public NoteGenerationService(@Qualifier("geminiService") AiServiceProvider geminiProvider,
                                 NoteRepository noteRepository,
                                 VideoProcessingService videoProcessingService) {
        this.geminiProvider = geminiProvider;
        this.noteRepository = noteRepository;
        this.videoProcessingService = videoProcessingService;
    }

    @Async("taskExecutor")
    public void generateNotesForVideo(Long userId, String videoUrl, VideoLinkRequest.GenerationMode mode) {
        System.out.println("--- Starting FINAL MULTIMODAL Pipeline Task with Mode: " + mode + " ---");
        File videoFile = new File("test.mp4");
        File audioFile = null;

        try {
            if (!videoFile.exists()) {
                throw new RuntimeException("FATAL: test.mp4 not found in project root.");
            }
            System.out.println("Step 1: Using local video file: " + videoFile.getAbsolutePath());

            audioFile = videoProcessingService.extractAudio(videoFile);
            System.out.println("Step 2: Audio extracted successfully: " + audioFile.getAbsolutePath());

            String generatedNotes = ((GeminiService) geminiProvider).generateNotesFromAudio(audioFile, mode);

            System.out.println("----- NOTES GENERATED (Mode: " + mode + ") -----");
            System.out.println(generatedNotes);
            System.out.println("------------------------------------");

            Note note = new Note();
            note.setUserId(userId);
            note.setVideoUrl("local file: test.mp4");
            note.setContent(generatedNotes);
            Note savedNote = noteRepository.save(note);
            System.out.println("Notes saved successfully with ID: " + savedNote.getId());

        } catch (Exception e) {
            System.err.println("An error occurred during the final pipeline: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
                System.out.println("Cleaned up temporary audio file: " + audioFile.getName());
            }
        }
    }
}