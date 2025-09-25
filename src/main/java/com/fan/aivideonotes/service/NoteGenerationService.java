package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.repository.TaskRepository;
import com.fan.aivideonotes.service.llm.LLMService; // 【注意】导入新的接口
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NoteGenerationService {

    // 注入新的 LLMServiceProvider
    private final LLMServiceProvider llmServiceProvider;
    private final NoteRepository noteRepository;
    private final VideoProcessingService videoProcessingService;
    private final TaskRepository taskRepository;

    @Autowired
    public NoteGenerationService(LLMServiceProvider llmServiceProvider,
                                 NoteRepository noteRepository,
                                 VideoProcessingService videoProcessingService,
                                 TaskRepository taskRepository) {
        this.llmServiceProvider = llmServiceProvider;
        this.noteRepository = noteRepository;
        this.videoProcessingService = videoProcessingService;
        this.taskRepository = taskRepository;
    }

    /**
     * [重构后]
     * 异步为视频生成笔记。
     * 此方法现在通过 LLMServiceProvider 动态选择 AI 模型。
     *
     * @param taskId      任务的唯一ID
     * @param request     包含视频URL、用户ID、模式等信息的请求对象
     */
    @Async("taskExecutor")
    @Transactional
    public void generateNotesForVideo(String taskId, VideoLinkRequest request) {

        updateTaskStatus(taskId, "PROCESSING", "Starting video processing...");

        File videoFile = null;
        File audioFile = null;
        File tempDirectory = null;

        try {
            updateTaskStatus(taskId, "PROCESSING", "Step 1: Downloading video from URL...");
            videoFile = videoProcessingService.downloadVideo(request.getUrl());
            tempDirectory = videoFile.getParentFile();

            updateTaskStatus(taskId, "PROCESSING", "Step 2: Extracting audio...");
            audioFile = videoProcessingService.extractAudio(videoFile);

            updateTaskStatus(taskId, "PROCESSING", "Step 3: Generating notes from audio with AI...");

            // 通过 provider 动态获取 LLM 服务并调用
            // 注意：request.getProvider() 暂时可能为 null，工厂类会返回默认的 "GEMINI"
            LLMService selectedLlmService = llmServiceProvider.getProvider(request.getProvider());
            String generatedNotes = selectedLlmService.generateNotesFromAudio(audioFile, request);

            updateTaskStatus(taskId, "PROCESSING", "Step 4: Saving generated notes to the database...");
            Note note = new Note();
            note.setUserId(request.getUserId());
            note.setVideoUrl(request.getUrl());
            note.setContent(generatedNotes);
            Note savedNote = noteRepository.save(note);

            Task finalTask = taskRepository.findById(taskId).orElseThrow(() -> new IllegalStateException("Task not found with id: " + taskId));
            finalTask.setStatus("COMPLETED");
            finalTask.setStatusMessage("Note generated successfully.");
            finalTask.setResultNote(savedNote);
            taskRepository.save(finalTask);
            System.out.println("Task " + taskId + " completed successfully.");

        } catch (Exception e) {
            System.err.println("An error occurred during the pipeline for task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            updateTaskStatus(taskId, "FAILED", e.getMessage());
        } finally {
            if (tempDirectory != null && tempDirectory.exists()) {
                deleteDirectory(tempDirectory);
                System.out.println("Cleaned up temporary directory for task " + taskId);
            }
        }
    }

    private void updateTaskStatus(String taskId, String status, String message) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Attempted to update a non-existent task with id: " + taskId));
        task.setStatus(status);
        task.setStatusMessage(message);
        taskRepository.save(task);
    }

    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
}